package net.shinyshoe.storagetracker.tracking;

import net.shinyshoe.storagetracker.StorageTracker;
import net.shinyshoe.storagetracker.config.ModConfig;
import net.shinyshoe.storagetracker.storage.StorageDatabase;
import net.shinyshoe.storagetracker.storage.StorageType;
import net.shinyshoe.storagetracker.storage.TrackedStorage;
import net.shinyshoe.storagetracker.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ClientStorageTracking {

	private static GlobalPos enderChestKeyCache;

	private static GlobalPos enderChestKey() {
		if (enderChestKeyCache == null) enderChestKeyCache = GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO);
		return enderChestKeyCache;
	}

	private static final long PENDING_INTERACTION_WINDOW_MILLIS = 3000;
	private static final long INVALIDATION_SWEEP_INTERVAL_MILLIS = 5000;
	private static final long SAVE_DEBOUNCE_MILLIS = 10_000;

	private static GlobalPos pendingInteractionPos;
	private static long pendingInteractionAtMillis;
	private static GlobalPos openContainerId;

	private static long lastSweepMillis;
	private static long lastSaveAttemptMillis;
	private static long sessionStartMillis;

	private ClientStorageTracking() {
	}

	public static void onRightClickBlock(Level level, BlockPos pos) {
		if (!level.isClientSide()) return;
		pendingInteractionPos = GlobalPos.of(level.dimension(), pos.immutable());
		pendingInteractionAtMillis = System.currentTimeMillis();
	}

	public static void onScreenOpened(Screen screen) {
		openContainerId = null;
		if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;
		if (pendingInteractionPos == null) return;
		if (System.currentTimeMillis() - pendingInteractionAtMillis > PENDING_INTERACTION_WINDOW_MILLIS) return;

		try {
			Minecraft client = Minecraft.getInstance();
			ClientLevel level = client.level;
			if (level == null || !level.dimension().equals(pendingInteractionPos.dimension())) return;

			BlockPos pos = pendingInteractionPos.pos();
			BlockState state = level.getBlockState(pos);
			Optional<StorageType> type = resolveStorageType(state);
			if (type.isEmpty()) return;

			boolean virtual = type.get() == StorageType.ENDER_CHEST;
			GlobalPos containerId = virtual
				? enderChestKey()
				: GlobalPos.of(level.dimension(), canonicalChestPos(level, pos, state));

			StorageDatabase db = StorageTracker.database();
			TrackedStorage existing = db.storages().get(containerId).orElse(null);
			String customName = virtual ? null : detectCustomName(containerScreen.getTitle(), state.getBlock());

			if (existing == null && !shouldTrackNewlySeen(type.get(), virtual, customName, StorageTracker.config())) {
				return;
			}

			long now = System.currentTimeMillis();
			TrackedStorage record;
			if (virtual) {
				record = existing != null ? existing.seenNow(now) : TrackedStorage.virtualStorage(containerId, type.get(), now);
			} else {
				record = existing != null
					? existing.withCustomName(customName).seenNow(now)
					: TrackedStorage.newlySeen(containerId, type.get(), customName, now);
			}
			db.storages().add(record);
			db.markDirty();
			openContainerId = containerId;
		} catch (RuntimeException e) {
			StorageTracker.LOGGER.error("Failed to record opened storage", e);
			openContainerId = null;
		}
	}

	static boolean shouldTrackNewlySeen(StorageType type, boolean virtual, @Nullable String customName, ModConfig config) {
		if (config.excludedTypes().contains(type)) return false;
		if (virtual) return config.trackEnderChest();
		return !config.trackOnlyNamed() || customName != null;
	}

	public static void onScreenClosed(Screen screen) {
		GlobalPos containerId = openContainerId;
		openContainerId = null;
		if (containerId == null || !(screen instanceof AbstractContainerScreen<?> containerScreen)) return;

		try {
			Minecraft client = Minecraft.getInstance();
			Player player = client.player;
			if (player == null) return;

			AbstractContainerMenu menu = containerScreen.getMenu();
			List<ItemStack> stacks = new ArrayList<>();
			for (Slot slot : menu.slots) {
				if (slot.container == player.getInventory()) continue;
				ItemStack stack = slot.getItem();
				if (!stack.isEmpty()) stacks.add(stack.copy());
			}

			StorageTracker.database().replaceContainerContents(containerId, stacks, StorageTracker.clientRegistries());
		} catch (RuntimeException e) {
			StorageTracker.LOGGER.error("Failed to record contents of closed storage", e);
		}
	}

	public static void onBlockBroken(Level level, BlockPos pos, BlockState stateBeforeBreak) {
		if (!level.isClientSide()) return;
		try {
			Optional<StorageType> type = resolveStorageType(stateBeforeBreak);
			if (type.isEmpty()) return;

			BlockPos canonical = canonicalChestPos(level, pos, stateBeforeBreak);
			GlobalPos containerId = GlobalPos.of(level.dimension(), canonical);
			StorageDatabase db = StorageTracker.database();
			if (db.storages().contains(containerId)) {
				db.storages().remove(containerId);
				db.contents().remove(containerId);
				db.markDirty();
			}
		} catch (RuntimeException e) {
			StorageTracker.LOGGER.error("Failed to invalidate broken storage at {}", pos, e);
		}
	}

	public static void tick(Minecraft client) {
		long now = System.currentTimeMillis();
		if (now - lastSweepMillis >= INVALIDATION_SWEEP_INTERVAL_MILLIS) {
			lastSweepMillis = now;
			sweepInvalidation(client);
			sweepTimeBasedStaleness(now);
		}

		StorageDatabase db = StorageTracker.database();
		if (db.isDirty() && now - lastSaveAttemptMillis >= SAVE_DEBOUNCE_MILLIS) {
			lastSaveAttemptMillis = now;
			db.save();
		}
	}

	public static void onWorldJoined() {
		sessionStartMillis = System.currentTimeMillis();
		StorageTracker.database().load();
	}

	public static void onWorldLeft() {
		StorageTracker.database().saveNow();
		StorageTracker.database().reset();
		pendingInteractionPos = null;
		openContainerId = null;
	}

	private static void sweepInvalidation(Minecraft client) {
		ClientLevel level = client.level;
		if (level == null) return;
		StorageDatabase db = StorageTracker.database();

		for (TrackedStorage storage : List.copyOf(db.storages().getAll())) {
			if (storage.virtual()) continue;
			GlobalPos globalPos = storage.pos();
			if (!globalPos.dimension().equals(level.dimension())) continue;

			BlockPos blockPos = globalPos.pos();
			if (!level.hasChunkAt(blockPos)) continue;

			Optional<StorageType> resolved = resolveStorageType(level.getBlockState(blockPos));
			boolean matches = resolved.isPresent() && resolved.get() == storage.type();

			if (matches) {
				if (storage.stale()) {
					db.storages().add(storage.seenNow(System.currentTimeMillis()));
					db.markDirty();
				}
				continue;
			}

			if (storage.stale()) {
				db.storages().remove(globalPos);
				db.contents().remove(globalPos);
			} else {
				db.storages().add(storage.markStale());
				db.contents().remove(globalPos);
			}
			db.markDirty();
		}
	}

	static boolean isTimeStale(long lastUpdated, ModConfig config, long sessionStartMillis, long now) {
		if (config.staleAfterMinutes() <= 0) return false;
		long staleAfterMillis = config.staleAfterMinutes() * 60_000L;
		long since = switch (config.staleTimeMode()) {
			case REAL_TIME -> lastUpdated;
			case SESSION_TIME -> Math.max(lastUpdated, sessionStartMillis);
		};
		return now - since >= staleAfterMillis;
	}

	private static void sweepTimeBasedStaleness(long now) {
		ModConfig config = StorageTracker.config();
		if (config.staleAfterMinutes() <= 0) return;

		StorageDatabase db = StorageTracker.database();
		for (TrackedStorage storage : List.copyOf(db.storages().getAll())) {
			if (!isTimeStale(storage.lastUpdated(), config, sessionStartMillis, now)) continue;

			if (storage.stale()) {
				db.storages().remove(storage.pos());
				db.contents().remove(storage.pos());
			} else {
				db.storages().add(storage.markStale());
				db.contents().remove(storage.pos());
			}
			db.markDirty();
		}
	}

	private static Optional<StorageType> resolveStorageType(BlockState state) {
		var id = RegistryUtil.blockId(state.getBlock());
		return id == null ? Optional.empty() : StorageType.byId(id);
	}

	private static String detectCustomName(Component title, Block block) {
		String titleText = title.getString();
		String defaultText = Component.translatable(block.getDescriptionId()).getString();
		return titleText.equals(defaultText) ? null : titleText;
	}

	private static BlockPos canonicalChestPos(Level level, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof ChestBlock) || !state.hasProperty(ChestBlock.TYPE)) return pos;
		ChestType type = state.getValue(ChestBlock.TYPE);
		if (type == ChestType.SINGLE) return pos;

		ChestType partnerType = type == ChestType.LEFT ? ChestType.RIGHT : ChestType.LEFT;
		Direction facing = state.hasProperty(ChestBlock.FACING) ? state.getValue(ChestBlock.FACING) : null;

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos neighborPos = pos.relative(dir);
			BlockState neighborState = level.getBlockState(neighborPos);
			if (neighborState.getBlock() != state.getBlock() || !neighborState.hasProperty(ChestBlock.TYPE)) continue;
			if (neighborState.getValue(ChestBlock.TYPE) != partnerType) continue;
			if (facing != null && (!neighborState.hasProperty(ChestBlock.FACING) || neighborState.getValue(ChestBlock.FACING) != facing)) {
				continue;
			}
			return pos.asLong() <= neighborPos.asLong() ? pos : neighborPos;
		}
		return pos;
	}
}
