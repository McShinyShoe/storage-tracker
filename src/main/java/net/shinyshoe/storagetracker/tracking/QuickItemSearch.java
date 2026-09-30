package net.shinyshoe.storagetracker.tracking;

import net.shinyshoe.storagetracker.StorageTracker;
import net.shinyshoe.storagetracker.client.render.WorldOverlays;
import net.shinyshoe.storagetracker.config.ModConfig;
import net.shinyshoe.storagetracker.storage.StorageDatabase;
import net.shinyshoe.storagetracker.storage.TrackedStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public final class QuickItemSearch {

	private QuickItemSearch() {
	}

	public static void onQuickSearchPressed(Minecraft client) {
		if (!(client.screen instanceof AbstractContainerScreen<?> containerScreen)) return;

		Slot hovered = hoveredSlot(containerScreen);
		if (hovered == null) return;
		ItemStack stack = hovered.getItem();
		if (stack.isEmpty()) return;

		StorageDatabase db = StorageTracker.database();
		OptionalInt itemId = db.lookupItem(stack, StorageTracker.clientRegistries());
		List<GlobalPos> positions = itemId.isPresent() ? inScopeContainers(db, itemId.getAsInt(), client) : List.of();

		if (positions.isEmpty()) {
			sendMessage(client, "message.storage-tracker.quick_search.not_found", stack.getHoverName());
			return;
		}

		ModConfig config = StorageTracker.config();
		WorldOverlays.highlights().highlightAll(positions, System.currentTimeMillis(), config.highlightDurationSeconds() * 1000L);
		sendMessage(client, "message.storage-tracker.quick_search.found", stack.getHoverName(), positions.size());
	}

	private static Slot hoveredSlot(AbstractContainerScreen<?> screen) {
		//? fabric {
		return screen.hoveredSlot;
		//?} neoforge {
		/*return screen.getSlotUnderMouse();
		 *///?} forge {
		/*return screen.getSlotUnderMouse();
		 *///?}
	}

	private static List<GlobalPos> inScopeContainers(StorageDatabase db, int itemId, Minecraft client) {
		ResourceKey<Level> currentDim = client.level != null ? client.level.dimension() : null;
		List<GlobalPos> positions = new ArrayList<>();
		for (GlobalPos pos : db.contents().containersOf(itemId)) {
			TrackedStorage storage = db.storages().get(pos).orElse(null);
			if (storage == null || storage.virtual()) continue;
			if (currentDim != null && !pos.dimension().equals(currentDim)) continue;
			positions.add(pos);
		}
		return positions;
	}

	private static void sendMessage(Minecraft client, String key, Object... args) {
		if (client.player == null) return;
		Component message = Component.translatable(key, args);
		//? if < 26.1.2 {
		client.player.displayClientMessage(message, false);
		//?} else {
		/*client.player.sendSystemMessage(message);
		*///?}
	}
}
