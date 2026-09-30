package net.shinyshoe.storagetracker.platform.neoforge;

//? neoforge {

/*import net.shinyshoe.storagetracker.StorageTracker;
import net.shinyshoe.storagetracker.client.ModKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = StorageTracker.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEventSubscriber {
	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event) {
		StorageTracker.onInitializeClient();
	}

	@SubscribeEvent
	public static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
		event.register(ModKeyMappings.OPEN_SEARCH);
		event.register(ModKeyMappings.QUICK_SEARCH);
	}
}
*///?}
