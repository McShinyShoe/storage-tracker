package net.shinyshoe.storagetracker;

import net.shinyshoe.storagetracker.config.ModConfig;
import net.shinyshoe.storagetracker.config.ModConfigManager;
import net.shinyshoe.storagetracker.platform.Platform;
import net.shinyshoe.storagetracker.storage.StorageDatabase;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? fabric {
import net.shinyshoe.storagetracker.platform.fabric.FabricPlatform;
//?} neoforge {
/*import net.shinyshoe.storagetracker.platform.neoforge.NeoforgePlatform;
 *///?} forge {
/*import net.shinyshoe.storagetracker.platform.forge.ForgePlatform;
 *///?}

@SuppressWarnings("LoggingSimilarMessage")
public class StorageTracker {

	public static final String MOD_ID = /*$ mod_id*/ "modtemplate";
	public static final String MOD_VERSION = /*$ mod_version*/ "0.1.0";
	public static final String MOD_FRIENDLY_NAME = /*$ mod_name*/ "Mod Template";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Platform PLATFORM = createPlatformInstance();

	private static StorageDatabase database;

	public static void onInitialize() {
		LOGGER.info("Initializing {} on {}", MOD_ID, StorageTracker.xplat().loader());
		LOGGER.debug("{}: { version: {}; friendly_name: {} }", MOD_ID, MOD_VERSION, MOD_FRIENDLY_NAME);
	}

	public static void onInitializeClient() {
		LOGGER.info("Initializing {} Client on {}", MOD_ID, StorageTracker.xplat().loader());
		LOGGER.debug("{}: { version: {}; friendly_name: {} }", MOD_ID, MOD_VERSION, MOD_FRIENDLY_NAME);

		ModConfigManager.load();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> database().saveNow(), "StorageTracker-ShutdownSave"));
	}

	public static Platform xplat() {
		return PLATFORM;
	}

	public static StorageDatabase database() {
		if (database == null) database = new StorageDatabase();
		return database;
	}

	public static ModConfig config() {
		return ModConfigManager.current();
	}

	public static Object clientRegistries() {
		//? if > 1.19.2 {
		Minecraft client = Minecraft.getInstance();
		return client.level != null ? client.level.registryAccess() : null;
		//?} else {
		/*return null;
		*///?}
	}

	private static Platform createPlatformInstance() {
		//? fabric {
		return new FabricPlatform();
		//?} neoforge {
		/*return new NeoforgePlatform();
		 *///?} forge {
		/*return new ForgePlatform();
		 *///?}
	}

	public static ResourceLocation id(String path) {
		//? > 1.19.2 {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
		 //?} <= 1.19.2 {
		/*return new ResourceLocation(MOD_ID, path);
		*///?}
	}

	public static ResourceLocation id(String namespace, String path) {
		//? > 1.19.2 {
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
		 //?} <= 1.19.2 {
		/*return new ResourceLocation(namespace, path);
		*///?}
	}
}
