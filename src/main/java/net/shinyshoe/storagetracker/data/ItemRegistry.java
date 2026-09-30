package net.shinyshoe.storagetracker.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;

public final class ItemRegistry<K> {
	private final Map<K, Integer> idByCanonical = new LinkedHashMap<>();
	private final Map<Integer, K> canonicalById = new LinkedHashMap<>();
	private final Map<Integer, String> searchTextById = new HashMap<>();
	private int nextId = 1;

	public int getOrAssignId(K canonical, Supplier<String> searchTextIfNew) {
		Objects.requireNonNull(canonical, "canonical");
		Integer existing = idByCanonical.get(canonical);
		if (existing != null) return existing;

		int id = nextId++;
		idByCanonical.put(canonical, id);
		canonicalById.put(id, canonical);
		searchTextById.put(id, searchTextIfNew.get());
		return id;
	}

	public void assignId(int id, K canonical, String searchText) {
		Objects.requireNonNull(canonical, "canonical");
		idByCanonical.put(canonical, id);
		canonicalById.put(id, canonical);
		searchTextById.put(id, searchText);
		if (id >= nextId) nextId = id + 1;
	}

	public OptionalInt idOf(K canonical) {
		Integer id = idByCanonical.get(canonical);
		return id == null ? OptionalInt.empty() : OptionalInt.of(id);
	}

	public Optional<K> canonicalOf(int id) {
		return Optional.ofNullable(canonicalById.get(id));
	}

	public Optional<String> searchTextOf(int id) {
		return Optional.ofNullable(searchTextById.get(id));
	}

	public boolean contains(int id) {
		return canonicalById.containsKey(id);
	}

	public void updateSearchText(int id, String searchText) {
		if (canonicalById.containsKey(id)) searchTextById.put(id, searchText);
	}

	public Set<Integer> ids() {
		return Collections.unmodifiableSet(canonicalById.keySet());
	}

	public int size() {
		return canonicalById.size();
	}

	public Set<Integer> retainOnly(Set<Integer> referencedIds) {
		Set<Integer> removed = new LinkedHashSet<>();
		Iterator<Map.Entry<Integer, K>> it = canonicalById.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, K> entry = it.next();
			if (!referencedIds.contains(entry.getKey())) {
				removed.add(entry.getKey());
				idByCanonical.remove(entry.getValue());
				searchTextById.remove(entry.getKey());
				it.remove();
			}
		}
		return removed;
	}

	public void clear() {
		idByCanonical.clear();
		canonicalById.clear();
		searchTextById.clear();
		nextId = 1;
	}
}
