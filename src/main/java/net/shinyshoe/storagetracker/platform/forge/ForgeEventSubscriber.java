package net.shinyshoe.storagetracker.platform.forge;

//? forge {

/*import net.shinyshoe.storagetracker.command.ModCommands;
import net.shinyshoe.storagetracker.event.ExampleEventHandler; // sample_content
import net.minecraft.server.level.ServerPlayer; // sample_content
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ForgeEventSubscriber {

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		ModCommands.register(event.getDispatcher());
	}

	@SubscribeEvent // sample_content
	public static void onPlayerDamage(LivingDamageEvent event) { // sample_content
		if (event.getEntity() instanceof ServerPlayer player && event.getAmount() > 0) { // sample_content
			ExampleEventHandler.onPlayerHurt(player); // sample_content
		} // sample_content
	} // sample_content
}
*///?}
