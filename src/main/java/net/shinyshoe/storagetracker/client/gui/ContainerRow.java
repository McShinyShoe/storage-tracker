package net.shinyshoe.storagetracker.client.gui;

import net.shinyshoe.storagetracker.storage.TrackedStorage;
import org.jetbrains.annotations.Nullable;

public record ContainerRow(TrackedStorage storage, int count, @Nullable Double distance) {
}
