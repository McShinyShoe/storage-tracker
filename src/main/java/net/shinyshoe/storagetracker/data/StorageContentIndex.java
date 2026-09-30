package net.shinyshoe.storagetracker.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// what is inside each of the containers
public final class StorageContentIndex<C> {
	private final Map<C, ContainerContents> byContainer = new LinkedHashMap<>();
	private final Map<Integer, Set<C>> byItem = new HashMap<>();

	public ContainerContents contentsOf(C containerId) {
		ContainerContents existing = byContainer.get(containerId);
		return existing == null ? new ContainerContents() : existing.copy();
	}

	public boolean hasContainer(C containerId) {
		return byContainer.containsKey(containerId);
	}

	public void replace(C containerId, ContainerContents newContents) {
		ContainerContents old = byContainer.get(containerId);
		if (old != null) {
			for (int itemId : old.asMap().keySet()) {
				removeFromInverse(itemId, containerId);
			}
		}

		if (newContents == null || newContents.isEmpty()) {
			byContainer.remove(containerId);
			return;
		}

		byContainer.put(containerId, newContents.copy());
		for (int itemId : newContents.asMap().keySet()) {
			byItem.computeIfAbsent(itemId, k -> new LinkedHashSet<>()).add(containerId);
		}
	}

	public void remove(C containerId) {
		replace(containerId, null);
	}

	private void removeFromInverse(int itemId, C containerId) {
		Set<C> set = byItem.get(itemId);
		if (set == null) return;
		set.remove(containerId);
		if (set.isEmpty()) byItem.remove(itemId);
	}

	public Set<C> containersOf(int itemId) {
		return Collections.unmodifiableSet(byItem.getOrDefault(itemId, Set.of()));
	}

	public Set<Integer> allItemIds() {
		return Collections.unmodifiableSet(byItem.keySet());
	}

	public Set<C> allContainers() {
		return Collections.unmodifiableSet(byContainer.keySet());
	}

	public long totalCount(int itemId) {
		long total = 0;
		for (C container : containersOf(itemId)) {
			total += contentsOf(container).get(itemId);
		}
		return total;
	}

	public void rebuildInverseIndex() {
		byItem.clear();
		for (Map.Entry<C, ContainerContents> entry : byContainer.entrySet()) {
			for (int itemId : entry.getValue().asMap().keySet()) {
				byItem.computeIfAbsent(itemId, k -> new LinkedHashSet<>()).add(entry.getKey());
			}
		}
	}

	public void clear() {
		byContainer.clear();
		byItem.clear();
	}
}
