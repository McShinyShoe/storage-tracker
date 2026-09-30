package net.shinyshoe.storagetracker.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ContainerContents {
	private final Map<Integer, Integer> counts;

	public ContainerContents() {
		this.counts = new LinkedHashMap<>();
	}

	private ContainerContents(Map<Integer, Integer> counts) {
		this.counts = new LinkedHashMap<>(counts);
	}

	public static ContainerContents of(Map<Integer, Integer> source) {
		ContainerContents contents = new ContainerContents();
		source.forEach(contents::set);
		return contents;
	}

	public void add(int itemId, int amount) {
		if (amount <= 0) return;
		counts.merge(itemId, amount, (a, b) -> (int) Math.min((long) a + (long) b, Integer.MAX_VALUE));
	}

	public void set(int itemId, int amount) {
		if (amount <= 0) counts.remove(itemId);
		else counts.put(itemId, amount);
	}

	public int get(int itemId) {
		return counts.getOrDefault(itemId, 0);
	}

	public boolean isEmpty() {
		return counts.isEmpty();
	}

	public int distinctItemCount() {
		return counts.size();
	}

	public Map<Integer, Integer> asMap() {
		return Collections.unmodifiableMap(counts);
	}

	public ContainerContents copy() {
		return new ContainerContents(counts);
	}
}
