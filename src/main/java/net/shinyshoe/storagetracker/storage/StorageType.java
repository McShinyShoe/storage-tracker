package net.shinyshoe.storagetracker.storage;

import net.shinyshoe.storagetracker.StorageTracker;
import net.minecraft.resources.ResourceLocation;

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

	private final ResourceLocation id;

	StorageType(String namespace, String path) {
		this.id = StorageTracker.id(namespace, path);
	}

	public ResourceLocation getId() {
		return id;
	}
}
