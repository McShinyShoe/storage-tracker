package net.shinyshoe.storagetracker.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class VersionedNbtFileUtil {

	private VersionedNbtFileUtil() {
	}

	// i wish i could use rust enum...
	public record ReadResult(CompoundTag root, boolean existed, boolean corrupt) {
		static ReadResult missing() {
			return new ReadResult(new CompoundTag(), false, false);
		}

		static ReadResult recovered() {
			return new ReadResult(new CompoundTag(), true, true);
		}

		static ReadResult of(CompoundTag root) {
			return new ReadResult(root, true, false);
		}
	}

	public static ReadResult readOrRecover(Path file) {
		if (!Files.exists(file)) return ReadResult.missing();
		try {
			return ReadResult.of(NbtFileIOUtil.read(file));
		} catch (IOException | RuntimeException e) {
			backupCorruptFile(file);
			return ReadResult.recovered();
		}
	}

	public static int schemaVersion(CompoundTag root, String key) {
		Tag tag = root.get(key);
		return tag instanceof NumericTag numeric ? numericAsInt(numeric) : 0;
	}

	public static void writeAtomic(CompoundTag root, Path file) throws IOException {
		Files.createDirectories(file.getParent());
		Path tmp = file.resolveSibling(file.getFileName().toString() + ".tmp");
		NbtFileIOUtil.write(root, tmp);
		try {
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void backupCorruptFile(Path file) {
		try {
			Path backup = file.resolveSibling(file.getFileName().toString() + ".corrupt-" + System.currentTimeMillis());
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ignored) {
			// if even the backup fails, recovery (starting empty) still continue
		}
	}

	private static int numericAsInt(NumericTag tag) {
		//? if >= 1.21.7 {
		return tag.intValue();
		//?} else {
		/*return tag.getAsInt();
		*///?}
	}
}
