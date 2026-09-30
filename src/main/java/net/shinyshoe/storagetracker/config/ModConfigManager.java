package net.shinyshoe.storagetracker.config;

import net.shinyshoe.storagetracker.StorageTracker;
import net.shinyshoe.storagetracker.util.VersionedNbtFileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ModConfigManager {
	public static final int SCHEMA_VERSION = 1;

	private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "StorageTracker-ConfigIO");
		thread.setDaemon(true);
		return thread;
	});

	private static volatile ModConfig current = ModConfig.DEFAULT;

	private ModConfigManager() {
	}

	public static ModConfig current() {
		return current;
	}

	public static void set(ModConfig newConfig) {
		current = newConfig;
		save();
	}

	public static void reset() {
		set(ModConfig.DEFAULT);
	}

	public static void load() {
		Path file = configFile();
		VersionedNbtFileUtil.ReadResult result = VersionedNbtFileUtil.readOrRecover(file);
		if (result.corrupt()) {
			StorageTracker.LOGGER.error("Failed to read config from {}; using defaults (corrupt file backed up)", file);
			current = ModConfig.DEFAULT;
			return;
		}
		if (!result.existed()) {
			current = ModConfig.DEFAULT;
			return;
		}

		CompoundTag root = result.root();
		int fileVersion = VersionedNbtFileUtil.schemaVersion(root, "schema_version");
		if (fileVersion > SCHEMA_VERSION) {
			StorageTracker.LOGGER.warn(
				"{}: schema version {} is newer than this version of the mod supports ({}); attempting a best-effort load",
				file, fileVersion, SCHEMA_VERSION);
		}
		// No migrations yet :{ this is schema v1 too

		Tag configTag = root.get("config");
		current = configTag == null ? ModConfig.DEFAULT : ModConfig.CODEC.parse(NbtOps.INSTANCE, configTag)
			.resultOrPartial(error -> StorageTracker.LOGGER.error("Failed to parse config from {}: {}", file, error))
			.orElse(ModConfig.DEFAULT);
	}

	private static void save() {
		CompoundTag root = new CompoundTag();
		root.putInt("schema_version", SCHEMA_VERSION);
		ModConfig.CODEC.encodeStart(NbtOps.INSTANCE, current)
			.resultOrPartial(error -> StorageTracker.LOGGER.error("Failed to encode config: {}", error))
			.ifPresent(tag -> root.put("config", tag));

		Path file = configFile();
		IO_EXECUTOR.execute(() -> {
			try {
				VersionedNbtFileUtil.writeAtomic(root, file);
			} catch (IOException e) {
				StorageTracker.LOGGER.error("Failed to write config file {}", file, e);
			}
		});
	}

	private static Path configFile() {
		return Minecraft.getInstance().gameDirectory.toPath()
			.resolve(StorageTracker.MOD_ID)
			.resolve("config.nbt");
	}
}
