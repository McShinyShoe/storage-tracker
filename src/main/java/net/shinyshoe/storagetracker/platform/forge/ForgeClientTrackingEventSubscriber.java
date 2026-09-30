package net.shinyshoe.storagetracker.platform.forge;

//? forge {

/*import net.shinyshoe.storagetracker.StorageTracker;
import net.shinyshoe.storagetracker.client.ModKeyMappings;
import net.shinyshoe.storagetracker.client.render.WorldOverlays;
import net.shinyshoe.storagetracker.tracking.ClientStorageTracking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StorageTracker.MOD_ID, value = Dist.CLIENT)
public class ForgeClientTrackingEventSubscriber {

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
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (event.getLevel() instanceof Level level) {
			ClientStorageTracking.onBlockBroken(level, event.getPos(), event.getState());
		}
	}

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
		WorldOverlays.render(event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(),
			Minecraft.getInstance().gameRenderer.getMainCamera());
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
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
