package com.hardcorerevival.plugin;

import java.util.HashMap;
import java.util.Map;

/** Tracks which altars have had a villager sacrificed recently enough to still count. */
public final class RitualState {

    private final Map<String, Long> sacrificedAt = new HashMap<>();

    public void markSacrificed(String altarKey) {
        sacrificedAt.put(altarKey, System.currentTimeMillis());
    }

    public boolean hasRecentSacrifice(String altarKey, long windowMillis) {
        Long timestamp = sacrificedAt.get(altarKey);
        return timestamp != null && (System.currentTimeMillis() - timestamp) <= windowMillis;
    }

    public void clear(String altarKey) {
        sacrificedAt.remove(altarKey);
    }
}
