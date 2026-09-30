package net.shinyshoe.storagetracker.platform.fabric;

//? fabric {

import net.shinyshoe.storagetracker.StorageTracker;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		StorageTracker.onInitializeClient();
		FabricClientEventSubscriber.registerEvents();
	}

}
//?}
