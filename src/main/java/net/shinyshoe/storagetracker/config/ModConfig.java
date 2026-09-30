package net.shinyshoe.storagetracker.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.shinyshoe.storagetracker.storage.StorageType;

import java.util.List;
import java.util.Set;

public record ModConfig(
	boolean trackOnlyNamed,
	boolean trackEnderChest,
	Set<StorageType> excludedTypes,
	int staleAfterMinutes,
	StaleTimeMode staleTimeMode,
	int searchRadiusBlocks,
	boolean groupByContainer,
	LabelMode labelMode,
	int highlightDurationSeconds
) {

	public enum StaleTimeMode {
		REAL_TIME, SESSION_TIME;

		public static final Codec<StaleTimeMode> CODEC = Codec.STRING.xmap(StaleTimeMode::valueOf, StaleTimeMode::name);
	}

	public enum LabelMode {
		DISABLED, HOVERED, ALWAYS;

		public static final Codec<LabelMode> CODEC = Codec.STRING.xmap(LabelMode::valueOf, LabelMode::name);
	}

	public static final ModConfig DEFAULT = new ModConfig(
		false, true, Set.of(), 0, StaleTimeMode.REAL_TIME, 0, false, LabelMode.DISABLED, 5);

	private static final Codec<Set<StorageType>> STORAGE_TYPE_SET_CODEC =
		StorageType.CODEC.listOf().xmap(Set::copyOf, List::copyOf);

	public static final Codec<ModConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("track_only_named", DEFAULT.trackOnlyNamed()).forGetter(ModConfig::trackOnlyNamed),
		Codec.BOOL.optionalFieldOf("track_ender_chest", DEFAULT.trackEnderChest()).forGetter(ModConfig::trackEnderChest),
		STORAGE_TYPE_SET_CODEC.optionalFieldOf("excluded_types", DEFAULT.excludedTypes()).forGetter(ModConfig::excludedTypes),
		Codec.INT.optionalFieldOf("stale_after_minutes", DEFAULT.staleAfterMinutes()).forGetter(ModConfig::staleAfterMinutes),
		StaleTimeMode.CODEC.optionalFieldOf("stale_time_mode", DEFAULT.staleTimeMode()).forGetter(ModConfig::staleTimeMode),
		Codec.INT.optionalFieldOf("search_radius_blocks", DEFAULT.searchRadiusBlocks()).forGetter(ModConfig::searchRadiusBlocks),
		Codec.BOOL.optionalFieldOf("group_by_container", DEFAULT.groupByContainer()).forGetter(ModConfig::groupByContainer),
		LabelMode.CODEC.optionalFieldOf("label_mode", DEFAULT.labelMode()).forGetter(ModConfig::labelMode),
		Codec.INT.optionalFieldOf("highlight_duration_seconds", DEFAULT.highlightDurationSeconds()).forGetter(ModConfig::highlightDurationSeconds)
	).apply(instance, ModConfig::new));

	public ModConfig withTrackOnlyNamed(boolean value) {
		return new ModConfig(value, trackEnderChest, excludedTypes, staleAfterMinutes, staleTimeMode, searchRadiusBlocks, groupByContainer, labelMode, highlightDurationSeconds);
	}

	public ModConfig withTrackEnderChest(boolean value) {
		return new ModConfig(trackOnlyNamed, value, excludedTypes, staleAfterMinutes, staleTimeMode, searchRadiusBlocks, groupByContainer, labelMode, highlightDurationSeconds);
	}

	public ModConfig withExcludedTypes(Set<StorageType> value) {
		return new ModConfig(trackOnlyNamed, trackEnderChest, Set.copyOf(value), staleAfterMinutes, staleTimeMode, searchRadiusBlocks, groupByContainer, labelMode, highlightDurationSeconds);
	}

	public ModConfig withStaleAfterMinutes(int value) {
		return new ModConfig(trackOnlyNamed, trackEnderChest, excludedTypes, value, staleTimeMode, searchRadiusBlocks, groupByContainer, labelMode, highlightDurationSeconds);
	}

	public ModConfig withStaleTimeMode(StaleTimeMode value) {
		return new ModConfig(trackOnlyNamed, trackEnderChest, excludedTypes, staleAfterMinutes, value, searchRadiusBlocks, groupByContainer, labelMode, highlightDurationSeconds);
	}

	public ModConfig withSearchRadiusBlocks(int value) {
		return new ModConfig(trackOnlyNamed, trackEnderChest, excludedTypes, staleAfterMinutes, staleTimeMode, value, groupByContainer, labelMode, highlightDurationSeconds);
	}

	public ModConfig withGroupByContainer(boolean value) {
		return new ModConfig(trackOnlyNamed, trackEnderChest, excludedTypes, staleAfterMinutes, staleTimeMode, searchRadiusBlocks, value, labelMode, highlightDurationSeconds);
	}

	public ModConfig withLabelMode(LabelMode value) {
		return new ModConfig(trackOnlyNamed, trackEnderChest, excludedTypes, staleAfterMinutes, staleTimeMode, searchRadiusBlocks, groupByContainer, value, highlightDurationSeconds);
	}

	public ModConfig withHighlightDurationSeconds(int value) {
		return new ModConfig(trackOnlyNamed, trackEnderChest, excludedTypes, staleAfterMinutes, staleTimeMode, searchRadiusBlocks, groupByContainer, labelMode, value);
	}
}
