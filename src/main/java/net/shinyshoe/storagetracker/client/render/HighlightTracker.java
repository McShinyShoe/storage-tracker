package net.shinyshoe.storagetracker.client.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HighlightTracker<K> {

	private final Map<K, Long> expiresAtMillis = new LinkedHashMap<>();

	public void highlight(K key, long nowMillis, long durationMillis) {
		expiresAtMillis.put(key, nowMillis + durationMillis);
	}

	public void highlightAll(Iterable<K> keys, long nowMillis, long durationMillis) {
		long expiresAt = nowMillis + durationMillis;
		for (K key : keys) {
			expiresAtMillis.put(key, expiresAt);
		}
	}

	public List<K> activeKeys(long nowMillis) {
		List<K> active = new ArrayList<>();
		expiresAtMillis.entrySet().removeIf(entry -> {
			boolean expired = entry.getValue() <= nowMillis;
			if (!expired) active.add(entry.getKey());
			return expired;
		});
		return active;
	}

	public void clear() {
		expiresAtMillis.clear();
	}
}
