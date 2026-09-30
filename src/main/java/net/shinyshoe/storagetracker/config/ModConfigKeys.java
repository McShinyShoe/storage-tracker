package net.shinyshoe.storagetracker.config;

import net.shinyshoe.storagetracker.storage.StorageType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModConfigKeys {

	private ModConfigKeys() {
	}

	public static List<String> keys() {
		return List.of(
			"track_only_named", "track_ender_chest", "excluded_types", "stale_after_minutes",
			"stale_time_mode", "search_radius_blocks", "group_by_container", "label_mode",
			"highlight_duration_seconds");
	}

	public static String describe(ModConfig config, String key) {
		return switch (normalize(key)) {
			case "track_only_named" -> String.valueOf(config.trackOnlyNamed());
			case "track_ender_chest" -> String.valueOf(config.trackEnderChest());
			case "excluded_types" -> formatTypeSet(config.excludedTypes());
			case "stale_after_minutes" -> String.valueOf(config.staleAfterMinutes());
			case "stale_time_mode" -> config.staleTimeMode().name().toLowerCase(Locale.ROOT);
			case "search_radius_blocks" -> String.valueOf(config.searchRadiusBlocks());
			case "group_by_container" -> String.valueOf(config.groupByContainer());
			case "label_mode" -> config.labelMode().name().toLowerCase(Locale.ROOT);
			case "highlight_duration_seconds" -> String.valueOf(config.highlightDurationSeconds());
			default -> throw new IllegalArgumentException("Unknown config key: " + key);
		};
	}

	public static ModConfig with(ModConfig config, String key, String rawValue) {
		String trimmed = rawValue.trim();
		return switch (normalize(key)) {
			case "track_only_named" -> config.withTrackOnlyNamed(parseBool(key, trimmed));
			case "track_ender_chest" -> config.withTrackEnderChest(parseBool(key, trimmed));
			case "excluded_types" -> config.withExcludedTypes(parseTypeSet(trimmed));
			case "stale_after_minutes" -> config.withStaleAfterMinutes(parseNonNegativeInt(key, trimmed));
			case "stale_time_mode" -> config.withStaleTimeMode(parseEnum(key, trimmed, ModConfig.StaleTimeMode.class));
			case "search_radius_blocks" -> config.withSearchRadiusBlocks(parseNonNegativeInt(key, trimmed));
			case "group_by_container" -> config.withGroupByContainer(parseBool(key, trimmed));
			case "label_mode" -> config.withLabelMode(parseEnum(key, trimmed, ModConfig.LabelMode.class));
			case "highlight_duration_seconds" -> config.withHighlightDurationSeconds(parseNonNegativeInt(key, trimmed));
			default -> throw new IllegalArgumentException("Unknown config key: " + key);
		};
	}

	private static String formatTypeSet(Set<StorageType> types) {
		if (types.isEmpty()) return "none";
		return types.stream().map(t -> t.name().toLowerCase(Locale.ROOT)).sorted().collect(Collectors.joining(","));
	}

	private static Set<StorageType> parseTypeSet(String trimmed) {
		if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("none")) return Set.of();
		Set<StorageType> result = new LinkedHashSet<>();
		for (String token : trimmed.split(",")) {
			String name = token.trim();
			if (name.isEmpty()) continue;
			try {
				result.add(StorageType.valueOf(name.toUpperCase(Locale.ROOT)));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Unknown storage type: " + name);
			}
		}
		return Set.copyOf(result);
	}

	private static boolean parseBool(String key, String trimmed) {
		if (trimmed.equalsIgnoreCase("true")) return true;
		if (trimmed.equalsIgnoreCase("false")) return false;
		throw new IllegalArgumentException("Expected true/false for " + key + ", got: " + trimmed);
	}

	private static int parseNonNegativeInt(String key, String trimmed) {
		int value;
		try {
			value = Integer.parseInt(trimmed);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Expected a whole number for " + key + ", got: " + trimmed);
		}
		if (value < 0) throw new IllegalArgumentException(key + " must not be negative");
		return value;
	}

	private static <E extends Enum<E>> E parseEnum(String key, String trimmed, Class<E> type) {
		try {
			return Enum.valueOf(type, trimmed.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unknown value for " + key + ": " + trimmed);
		}
	}

	private static String normalize(String key) {
		return key.trim().toLowerCase(Locale.ROOT);
	}
}
