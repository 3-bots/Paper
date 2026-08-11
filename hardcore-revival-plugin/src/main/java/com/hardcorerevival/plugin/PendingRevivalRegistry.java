package com.hardcorerevival.plugin;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/** Tracks players who died in a hardcore world and are awaiting the revival ritual. */
public final class PendingRevivalRegistry {

    private final HardcoreRevivalPlugin plugin;
    private final File file;
    private final Map<UUID, String> pending = new HashMap<>();

    public PendingRevivalRegistry(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "pending.yml");
        load();
    }

    public void addPending(UUID uuid, String name) {
        pending.put(uuid, name);
        save();
    }

    public void removePending(UUID uuid) {
        pending.remove(uuid);
        save();
    }

    public boolean isPending(UUID uuid) {
        return pending.containsKey(uuid);
    }

    public String nameOf(UUID uuid) {
        return pending.get(uuid);
    }

    /** Finds a pending player whose name appears anywhere in the given text (e.g. a ritual book's pages). */
    public UUID findMatchingName(String text) {
        String lower = text.toLowerCase();
        for (Map.Entry<UUID, String> entry : pending.entrySet()) {
            if (lower.contains(entry.getValue().toLowerCase())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            try {
                pending.put(UUID.fromString(key), yaml.getString(key));
            } catch (IllegalArgumentException ignored) {
                // skip malformed entry
            }
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : pending.entrySet()) {
            yaml.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to save pending revivals", ex);
        }
    }
}
