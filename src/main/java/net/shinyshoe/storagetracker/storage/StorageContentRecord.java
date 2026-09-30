package net.shinyshoe.storagetracker.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;

import java.util.Map;
import java.util.stream.Collectors;

// For each container, maps what item stored and how many of em
public record StorageContentRecord(GlobalPos pos, Map<Integer, Integer> counts) {

	private static final Codec<Map<Integer, Integer>> COUNTS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT)
		.xmap(
			byString -> byString.entrySet().stream()
				.collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), Map.Entry::getValue)),
			byId -> byId.entrySet().stream()
				.collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue))
		);

	public static final Codec<StorageContentRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		GlobalPos.CODEC.fieldOf("pos").forGetter(StorageContentRecord::pos),
		COUNTS_CODEC.fieldOf("counts").forGetter(StorageContentRecord::counts)
	).apply(instance, StorageContentRecord::new));
}
