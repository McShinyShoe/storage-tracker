package net.shinyshoe.storagetracker.storage;

import net.shinyshoe.storagetracker.StorageTracker;
import net.shinyshoe.storagetracker.data.ContainerContents;
import net.shinyshoe.storagetracker.data.ItemRegistry;
import net.shinyshoe.storagetracker.data.StorageContentIndex;
import net.shinyshoe.storagetracker.storage.item.TrackedItem;
import net.shinyshoe.storagetracker.util.ItemStackIOUtil;
import net.shinyshoe.storagetracker.util.SearchTextUtil;
import net.shinyshoe.storagetracker.util.VersionedNbtFileUtil;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Stores all the 3 saved data
public final class StorageDatabase {
	public static final int SCHEMA_VERSION = 1;

	private static final Codec<List<TrackedStorage>> STORAGE_LIST_CODEC = TrackedStorage.CODEC.listOf();
	private static final Codec<List<TrackedItem>> ITEM_LIST_CODEC = TrackedItem.CODEC.listOf();
	private static final Codec<List<StorageContentRecord>> CONTENT_LIST_CODEC = StorageContentRecord.CODEC.listOf();

	private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "StorageTracker-IO");
		thread.setDaemon(true);
		return thread;
	});

	private final TrackedStorageManager storages = new TrackedStorageManager();
	private final ItemRegistry<CompoundTag> items = new ItemRegistry<>();
	private final StorageContentIndex<GlobalPos> contents = new StorageContentIndex<>();
	private final Map<Integer, ItemStack> decodedCache = new HashMap<>();

	private String worldKey;
	private boolean dirty;

	public TrackedStorageManager storages() {
		return storages;
	}

	public ItemRegistry<CompoundTag> items() {
		return items;
	}

	public StorageContentIndex<GlobalPos> contents() {
		return contents;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void markDirty() {
		dirty = true;
	}

	public int internItem(ItemStack stack, Object registries) {
		CompoundTag canonical = ItemStackIOUtil.canonicalize(stack, registries);
		return items.getOrAssignId(canonical, () -> SearchTextUtil.build(stack));
	}

	public OptionalInt lookupItem(ItemStack stack, Object registries) {
		return items.idOf(ItemStackIOUtil.canonicalize(stack, registries));
	}

	public ItemStack decodeItem(int id, Object registries) {
		ItemStack cached = decodedCache.get(id);
		if (cached != null) return cached;
		ItemStack decoded = items.canonicalOf(id).map(tag -> ItemStackIOUtil.decode(tag, registries)).orElse(ItemStack.EMPTY);
		decodedCache.put(id, decoded);
		return decoded;
	}

	public void replaceContainerContents(GlobalPos containerId, Collection<ItemStack> stacks, Object registries) {
		ContainerContents newContents = new ContainerContents();
		for (ItemStack stack : stacks) {
			if (stack == null || stack.isEmpty()) continue;
			int id = internItem(stack, registries);
			newContents.add(id, stack.getCount());
		}
		contents.replace(containerId, newContents);
		markDirty();
	}

	private void clearUnused() {
		items.retainOnly(contents.allItemIds()).forEach(decodedCache::remove);
	}

	public void load() {
		storages.clear();
		items.clear();
		contents.clear();
		decodedCache.clear();

		worldKey = resolveWorldKey().orElse(null);
		if (worldKey == null) return;

		Path file = dataFile(worldKey);
		VersionedNbtFileUtil.ReadResult result = VersionedNbtFileUtil.readOrRecover(file);
		if (result.corrupt()) {
			StorageTracker.LOGGER.error("Failed to read storage data from {}; starting empty (corrupt file backed up)", file);
			return;
		}
		if (!result.existed()) return;

		CompoundTag root = result.root();
		int fileVersion = VersionedNbtFileUtil.schemaVersion(root, "schema_version");
		if (fileVersion > SCHEMA_VERSION) {
			StorageTracker.LOGGER.warn(
				"{}: schema version {} is newer than this version of the mod supports ({}); attempting a best-effort load",
				file, fileVersion, SCHEMA_VERSION);
		}
		// No migrations yet :{ this is schema v1

		Tag storagesTag = root.get("storages");
		if (storagesTag != null) {
			STORAGE_LIST_CODEC.parse(NbtOps.INSTANCE, storagesTag)
				.resultOrPartial(error -> StorageTracker.LOGGER.error("Failed to parse tracked storages from {}: {}", file, error))
				.ifPresent(list -> list.forEach(storages::add));
		}

		Tag itemsTag = root.get("items");
		if (itemsTag != null) {
			ITEM_LIST_CODEC.parse(NbtOps.INSTANCE, itemsTag)
				.resultOrPartial(error -> StorageTracker.LOGGER.error("Failed to parse item list from {}: {}", file, error))
				.ifPresent(list -> list.forEach(item -> items.assignId(item.id(), item.canonicalStack(), item.searchText())));
		}

		Tag contentsTag = root.get("contents");
		if (contentsTag != null) {
			CONTENT_LIST_CODEC.parse(NbtOps.INSTANCE, contentsTag)
				.resultOrPartial(error -> StorageTracker.LOGGER.error("Failed to parse container contents from {}: {}", file, error))
				.ifPresent(list -> list.forEach(record -> contents.replace(record.pos(), ContainerContents.of(record.counts()))));
		}
	}

	public void save() {
		if (!ensureWorldKey()) return;
		CompoundTag root = buildRootTag();
		Path file = dataFile(worldKey);
		IO_EXECUTOR.execute(() -> writeAtomicLogged(root, file));
		dirty = false;
	}

	// save but, now! async might not finish correctly
	public void saveNow() {
		if (!ensureWorldKey()) return;
		writeAtomicLogged(buildRootTag(), dataFile(worldKey));
		dirty = false;
	}

	private static void writeAtomicLogged(CompoundTag root, Path file) {
		try {
			VersionedNbtFileUtil.writeAtomic(root, file);
		} catch (IOException e) {
			StorageTracker.LOGGER.error("Failed to write storage data file {}", file, e);
		}
	}

	public void reset() {
		storages.clear();
		items.clear();
		contents.clear();
		decodedCache.clear();
		worldKey = null;
		dirty = false;
	}

	private CompoundTag buildRootTag() {
		clearUnused();

		CompoundTag root = new CompoundTag();
		root.putInt("schema_version", SCHEMA_VERSION);

		STORAGE_LIST_CODEC.encodeStart(NbtOps.INSTANCE, List.copyOf(storages.getAll()))
			.resultOrPartial(error -> StorageTracker.LOGGER.error("Failed to encode tracked storages: {}", error))
			.ifPresent(tag -> root.put("storages", tag));

		List<TrackedItem> itemList = items.ids().stream()
			.map(id -> new TrackedItem(id, items.canonicalOf(id).orElseThrow(), items.searchTextOf(id).orElse("")))
			.toList();
		ITEM_LIST_CODEC.encodeStart(NbtOps.INSTANCE, itemList)
			.resultOrPartial(error -> StorageTracker.LOGGER.error("Failed to encode item list: {}", error))
			.ifPresent(tag -> root.put("items", tag));

		List<StorageContentRecord> contentList = contents.allContainers().stream()
			.map(pos -> new StorageContentRecord(pos, contents.contentsOf(pos).asMap()))
			.toList();
		CONTENT_LIST_CODEC.encodeStart(NbtOps.INSTANCE, contentList)
			.resultOrPartial(error -> StorageTracker.LOGGER.error("Failed to encode container contents: {}", error))
			.ifPresent(tag -> root.put("contents", tag));

		return root;
	}

	private boolean ensureWorldKey() {
		if (worldKey == null) worldKey = resolveWorldKey().orElse(null);
		return worldKey != null;
	}

	private static Optional<String> resolveWorldKey() {
		Minecraft client = Minecraft.getInstance();

		ServerData server = client.getCurrentServer();
		if (server != null) {
			return Optional.of("server_" + sanitize(server.ip));
		}

		if (client.hasSingleplayerServer()) {
			IntegratedServer integrated = client.getSingleplayerServer();
			if (integrated != null) {
				String levelId = integrated.getWorldPath(LevelResource.ROOT).getFileName().toString();
				return Optional.of("world_" + sanitize(levelId));
			}
		}

		return Optional.empty();
	}

	private static String sanitize(String raw) {
		return raw.replaceAll("[^a-zA-Z0-9._-]", "_");
	}

	private static Path dataFile(String worldKey) {
		return Minecraft.getInstance().gameDirectory.toPath()
			.resolve(StorageTracker.MOD_ID)
			.resolve(worldKey)
			.resolve("data.nbt");
	}
}
