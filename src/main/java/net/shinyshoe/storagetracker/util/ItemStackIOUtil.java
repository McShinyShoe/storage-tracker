package net.shinyshoe.storagetracker.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.shinyshoe.storagetracker.StorageTracker;

//? if > 1.19.2 {
import net.minecraft.core.HolderLookup;
//?}

public final class ItemStackIOUtil {

	private ItemStackIOUtil() {
	}

	public static CompoundTag encode(ItemStack stack, Object registries) {
		//? if > 1.19.2 {
		HolderLookup.Provider provider = (HolderLookup.Provider) registries;
		return ItemStack.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack)
			.resultOrPartial(error -> StorageTracker.LOGGER.error("Failed to encode item stack: {}", error))
			.filter(tag -> tag instanceof CompoundTag)
			.map(tag -> (CompoundTag) tag)
			.orElseGet(CompoundTag::new);
		//?} else {
		/*return stack.save(new CompoundTag());
		*///?}
	}

	public static ItemStack decode(CompoundTag tag, Object registries) {
		//? if > 1.19.2 {
		HolderLookup.Provider provider = (HolderLookup.Provider) registries;
		return ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
			.resultOrPartial(error -> StorageTracker.LOGGER.error("Failed to decode item stack: {}", error))
			.orElse(ItemStack.EMPTY);
		//?} else {
		/*return ItemStack.of(tag);
		*///?}
	}

	public static CompoundTag canonicalize(ItemStack stack, Object registries) {
		ItemStack normalized = stack.copy();
		normalized.setCount(1);
		return encode(normalized, registries);
	}
}
