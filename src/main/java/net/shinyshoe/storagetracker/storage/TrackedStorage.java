package net.shinyshoe.storagetracker.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// An instance of a storage that is tracked
public record TrackedStorage(
	GlobalPos pos,
	StorageType type,
	@Nullable String customName,
	long firstSeen,
	long lastUpdated,
	boolean stale,
	boolean virtual
) {

	public static final Codec<TrackedStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		GlobalPos.CODEC.fieldOf("pos").forGetter(TrackedStorage::pos),
		StorageType.CODEC.fieldOf("type").forGetter(TrackedStorage::type),
		Codec.STRING.optionalFieldOf("custom_name").forGetter(storage -> Optional.ofNullable(storage.customName())),
		Codec.LONG.fieldOf("first_seen").forGetter(TrackedStorage::firstSeen),
		Codec.LONG.fieldOf("last_updated").forGetter(TrackedStorage::lastUpdated),
		Codec.BOOL.optionalFieldOf("stale", false).forGetter(TrackedStorage::stale),
		Codec.BOOL.optionalFieldOf("virtual", false).forGetter(TrackedStorage::virtual)
	).apply(instance, (pos, type, customName, firstSeen, lastUpdated, stale, virtual) ->
		new TrackedStorage(pos, type, customName.orElse(null), firstSeen, lastUpdated, stale, virtual)));

	public static TrackedStorage newlySeen(GlobalPos pos, StorageType type, @Nullable String customName, long now) {
		return new TrackedStorage(pos, type, customName, now, now, false, false);
	}

	public static TrackedStorage virtualStorage(GlobalPos sentinelPos, StorageType type, long now) {
		return new TrackedStorage(sentinelPos, type, null, now, now, false, true);
	}

	public TrackedStorage withCustomName(@Nullable String newCustomName) {
		return new TrackedStorage(pos, type, newCustomName, firstSeen, lastUpdated, stale, virtual);
	}

	public TrackedStorage seenNow(long now) {
		return new TrackedStorage(pos, type, customName, firstSeen, now, false, virtual);
	}

	public TrackedStorage markStale() {
		return stale ? this : new TrackedStorage(pos, type, customName, firstSeen, lastUpdated, true, virtual);
	}
}
