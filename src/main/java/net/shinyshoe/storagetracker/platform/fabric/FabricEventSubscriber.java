package net.shinyshoe.storagetracker.platform.fabric;

//? fabric {

import net.shinyshoe.storagetracker.command.ModCommands;
import net.shinyshoe.storagetracker.event.ExampleEventHandler; // sample_content
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents; // sample_content
import net.minecraft.server.level.ServerPlayer; // sample_content

public class FabricEventSubscriber {

	public static void registerEvents() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ModCommands.register(dispatcher));
		//? != 1.19.2 {
		// sample_content
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> { // sample_content
			if (entity instanceof ServerPlayer && damageTaken > 0) { // sample_content
				ExampleEventHandler.onPlayerHurt((ServerPlayer) entity); // sample_content
			} // sample_content
		}); // sample_content
		//?}
	}
}
//?}
