package net.shinyshoe.storagetracker.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Path;

final class NbtFileIOUtil {

	private NbtFileIOUtil() {
	}

	static void write(CompoundTag tag, Path file) throws IOException {
		//? if > 1.19.2 {
		NbtIo.write(tag, file);
		//?} else {
		/*NbtIo.write(tag, file.toFile());
		*///?}
	}

	static CompoundTag read(Path file) throws IOException {
		//? if > 1.19.2 {
		return NbtIo.read(file);
		//?} else {
		/*return NbtIo.read(file.toFile());
		*///?}
	}
}
