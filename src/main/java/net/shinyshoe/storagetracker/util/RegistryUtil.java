package net.shinyshoe.storagetracker.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class RegistryUtil {

	private RegistryUtil() {
	}

	@Nullable
	public static ResourceLocation blockId(Block block) {
		//? if > 1.19.2 {
		return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
		//?} else {
		/*return net.minecraft.core.Registry.BLOCK.getKey(block);
		*///?}
	}

	@Nullable
	public static ResourceLocation itemId(Item item) {
		//? if > 1.19.2 {
		return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
		//?} else {
		/*return net.minecraft.core.Registry.ITEM.getKey(item);
		*///?}
	}
}
