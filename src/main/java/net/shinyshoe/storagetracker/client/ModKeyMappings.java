package net.shinyshoe.storagetracker.client;

import net.shinyshoe.storagetracker.StorageTracker;
import net.shinyshoe.storagetracker.tracking.QuickItemSearch;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class ModKeyMappings {

	public static final String LEGACY_CATEGORY_ID = "key.categories." + StorageTracker.MOD_ID;
	public static final String OPEN_SEARCH_ID = "key.storage-tracker.open_search";
	public static final String QUICK_SEARCH_ID = "key.storage-tracker.quick_search";

	//? if >= 26.1.2 {
	/*
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(StorageTracker.id("open_search_category"));
	*///?}

	public static final KeyMapping OPEN_SEARCH = create(OPEN_SEARCH_ID, GLFW.GLFW_KEY_K);
	public static final KeyMapping QUICK_SEARCH = create(QUICK_SEARCH_ID, GLFW.GLFW_KEY_Y);

	private ModKeyMappings() {
	}

	public static void handleTick(Minecraft client) {
		while (OPEN_SEARCH.consumeClick()) {
			//? if < 26.1.2 {
			if (client.screen instanceof net.shinyshoe.storagetracker.client.gui.StorageSearchScreen) {
				client.setScreen(null);
			} else if (client.screen == null) {
				client.setScreen(new net.shinyshoe.storagetracker.client.gui.StorageSearchScreen());
			}
			//?} else {
			/*
			if (client.player != null) {
				client.player.sendSystemMessage(Component.translatable("message.storage-tracker.gui_unavailable"));
			}
			*///?}
		}

		while (QUICK_SEARCH.consumeClick()) {
			QuickItemSearch.onQuickSearchPressed(client);
		}
	}

	private static KeyMapping create(String id, int glfwKey) {
		//? if < 26.1.2 {
		return new KeyMapping(id, InputConstants.Type.KEYSYM, glfwKey, LEGACY_CATEGORY_ID);
		//?} else {
		/*return new KeyMapping(id, InputConstants.Type.KEYSYM, glfwKey, CATEGORY);
		*///?}
	}
}
