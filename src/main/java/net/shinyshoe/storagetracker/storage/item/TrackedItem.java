package net.shinyshoe.storagetracker.storage.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

// Create a new entry for every unique item
public record TrackedItem(int id, CompoundTag canonicalStack, String searchText) {

	public static final Codec<TrackedItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("id").forGetter(TrackedItem::id),
		CompoundTag.CODEC.fieldOf("stack").forGetter(TrackedItem::canonicalStack),
		Codec.STRING.fieldOf("search").forGetter(TrackedItem::searchText)
	).apply(instance, TrackedItem::new));
}
