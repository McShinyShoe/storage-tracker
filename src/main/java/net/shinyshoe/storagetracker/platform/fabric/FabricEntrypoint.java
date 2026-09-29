package net.shinyshoe.storagetracker.platform.fabric;

//? fabric {

import net.shinyshoe.storagetracker.StorageTracker;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		StorageTracker.onInitialize();
		FabricEventSubscriber.registerEvents();
	}
}
//?}
