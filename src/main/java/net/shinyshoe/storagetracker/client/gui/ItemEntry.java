package net.shinyshoe.storagetracker.client.gui;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ItemEntry(int itemId, ItemStack displayStack, long totalCount, int containerCount,
						 double nearestDistanceSq, @Nullable GlobalPos containerPos) {
}
