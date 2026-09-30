package net.shinyshoe.storagetracker.platform.neoforge;

//? neoforge {

/*import net.shinyshoe.storagetracker.StorageTracker;
import net.shinyshoe.storagetracker.client.ModKeyMappings;
import net.shinyshoe.storagetracker.client.render.WorldOverlays;
import net.shinyshoe.storagetracker.tracking.ClientStorageTracking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = StorageTracker.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientTrackingEventSubscriber {

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		ClientStorageTracking.onRightClickBlock(event.getLevel(), event.getPos());
	}

	@SubscribeEvent
	public static void onScreenOpened(ScreenEvent.Init.Post event) {
		ClientStorageTracking.onScreenOpened(event.getScreen());
	}

	@SubscribeEvent
	public static void onScreenClosing(ScreenEvent.Closing event) {
		ClientStorageTracking.onScreenClosed(event.getScreen());
	}

	@SubscribeEvent
	public static void onBlockBreak(/^? if < 26.1.2 {^/BlockEvent.BreakEvent/^?} else {^/net.neoforged.neoforge.event.level.block.BreakBlockEvent/^?}^/ event) {
		if (event.getLevel() instanceof Level level) {
			ClientStorageTracking.onBlockBroken(level, event.getPos(), event.getState());
		}
	}

	/^? if < 1.21.7 {^/
	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
		WorldOverlays.render(event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(),
			Minecraft.getInstance().gameRenderer.getMainCamera());
	}
	/^?} else {^/
	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
		WorldOverlays.render(event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(),
			Minecraft.getInstance().gameRenderer.getMainCamera());
	}
	/^?}^/

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft client = Minecraft.getInstance();
		ClientStorageTracking.tick(client);
		ModKeyMappings.handleTick(client);
	}

	@SubscribeEvent
	public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		ClientStorageTracking.onWorldJoined();
	}

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		ClientStorageTracking.onWorldLeft();
	}
}
*///?}
