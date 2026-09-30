package net.shinyshoe.storagetracker.storage;

import com.mojang.serialization.Codec;
import net.shinyshoe.storagetracker.StorageTracker;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum StorageType {

	CHEST("minecraft", "chest"),
	TRAPPED_CHEST("minecraft", "trapped_chest"),
	ENDER_CHEST("minecraft", "ender_chest"),
	COPPER_CHEST("minecraft", "copper_chest"),
	BARREL("minecraft", "barrel"),
	SHULKER_BOX("minecraft", "shulker_box"),
	HOPPER("minecraft", "hopper"),
	DISPENSER("minecraft", "dispenser"),
	DROPPER("minecraft", "dropper"),
	FURNACE("minecraft", "furnace"),
	BLAST_FURNACE("minecraft", "blast_furnace"),
	SMOKER("minecraft", "smoker"),
	BREWING_STAND("minecraft", "brewing_stand"),
	CAMPFIRE("minecraft", "campfire"),
	SOUL_CAMPFIRE("minecraft", "soul_campfire"),
	CHISELED_BOOKSHELF("minecraft", "chiseled_bookshelf"),
	DECORATED_POT("minecraft", "decorated_pot"),
	CRAFTER("minecraft", "crafter"),
	JUKEBOX("minecraft", "jukebox"),
	SHELF("minecraft", "shelf");

	public static final Codec<StorageType> CODEC = Codec.STRING.xmap(StorageType::valueOf, StorageType::name);

	private static final Map<ResourceLocation, StorageType> BY_ID = Arrays.stream(values())
		.collect(Collectors.toMap(StorageType::getId, Function.identity()));

	private final ResourceLocation id;

	StorageType(String namespace, String path) {
		this.id = StorageTracker.id(namespace, path);
	}

	public ResourceLocation getId() {
		return id;
	}

	/** Looks up which tracked storage type (if any) a block's registry id corresponds to. */
	public static Optional<StorageType> byId(ResourceLocation id) {
		return Optional.ofNullable(BY_ID.get(id));
	}
}
