package net.shinyshoe.storagetracker.client.render;

import net.shinyshoe.storagetracker.config.ModConfig;

public final class NameLabelRenderer {

	public static final double MAX_DISTANCE_SQ = 48.0 * 48.0;

	private NameLabelRenderer() {
	}

	static boolean shouldShowLabel(ModConfig.LabelMode mode, boolean hovered, double distanceSq, double maxDistanceSq) {
		if (mode == ModConfig.LabelMode.DISABLED) return false;
		if (distanceSq > maxDistanceSq) return false;
		return mode == ModConfig.LabelMode.ALWAYS || hovered;
	}
}
