package net.shinyshoe.storagetracker.platform.fabric;

//? fabric {

import net.shinyshoe.storagetracker.client.ModKeyMappings;
import net.shinyshoe.storagetracker.client.render.WorldOverlays;
import net.shinyshoe.storagetracker.tracking.ClientStorageTracking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
//? if < 26.1.2 {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
//?} else {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
*///?}

public class FabricClientEventSubscriber {

	public static void registerEvents() {
		//? if < 26.1.2 {
		KeyBindingHelper.registerKeyBinding(ModKeyMappings.OPEN_SEARCH);
		KeyBindingHelper.registerKeyBinding(ModKeyMappings.QUICK_SEARCH);
		//?} else {
		/*KeyMappingHelper.registerKeyMapping(ModKeyMappings.OPEN_SEARCH);
		KeyMappingHelper.registerKeyMapping(ModKeyMappings.QUICK_SEARCH);
		*///?}

		//? if < 26.1.2 {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx ->
			WorldOverlays.render(ctx.matrixStack(), ctx.consumers(), ctx.camera()));
		//?} else {
		/*LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(ctx ->
			WorldOverlays.render(ctx.poseStack(), ctx.bufferSource(), Minecraft.getInstance().gameRenderer.getMainCamera()));
		*///?}

		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			ClientStorageTracking.onRightClickBlock(level, hitResult.getBlockPos());
			return InteractionResult.PASS;
		});

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ClientStorageTracking.onScreenOpened(screen);
			ScreenEvents.remove(screen).register(ClientStorageTracking::onScreenClosed);
		});

		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) ->
			ClientStorageTracking.onBlockBroken(level, pos, state));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientStorageTracking.tick(client);
			ModKeyMappings.handleTick(client);
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientStorageTracking.onWorldJoined());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientStorageTracking.onWorldLeft());
	}
}
//?}
