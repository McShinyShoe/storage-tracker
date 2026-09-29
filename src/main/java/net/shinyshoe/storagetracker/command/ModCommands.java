package net.shinyshoe.storagetracker.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ModCommands {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("storage-tracker")
			.executes(context -> {
				//? > 1.19.2 {
				context.getSource().sendSuccess(() -> Component.literal("hello world"), false);
				//?} <= 1.19.2 {
				/*context.getSource().sendSuccess(Component.literal("hello world"), false);
				*///?}
				return 1;
			}));
	}
}
