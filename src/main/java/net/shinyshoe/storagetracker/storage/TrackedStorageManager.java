package net.shinyshoe.storagetracker.storage;

import net.minecraft.core.GlobalPos;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class TrackedStorageManager {

	private final Map<GlobalPos, TrackedStorage> storages = new LinkedHashMap<>();

	public void add(TrackedStorage storage) {
		storages.put(storage.pos(), storage);
	}

	public void remove(GlobalPos pos) {
		storages.remove(pos);
	}

	public Optional<TrackedStorage> get(GlobalPos pos) {
		return Optional.ofNullable(storages.get(pos));
	}

	public boolean contains(GlobalPos pos) {
		return storages.containsKey(pos);
	}

	public Collection<TrackedStorage> getAll() {
		return Collections.unmodifiableCollection(storages.values());
	}

	public int size() {
		return storages.size();
	}

	public void clear() {
		storages.clear();
	}
}
