package com.simplehomes.plugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class HomeManager {

    private final SimpleHomesPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, Map<String, Location>> cache = new HashMap<>();

    public HomeManager(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.dataFolder.mkdirs();
    }

    public Map<String, Location> getHomes(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadHomes);
    }

    public void saveHomes(UUID uuid) {
        Map<String, Location> homes = cache.get(uuid);
        if (homes == null) {
            return;
        }

        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, Location> entry : homes.entrySet()) {
            String path = "homes." + entry.getKey();
            Location loc = entry.getValue();
            yaml.set(path + ".world", loc.getWorld().getName());
            yaml.set(path + ".x", loc.getX());
            yaml.set(path + ".y", loc.getY());
            yaml.set(path + ".z", loc.getZ());
            yaml.set(path + ".yaw", loc.getYaw());
            yaml.set(path + ".pitch", loc.getPitch());
        }

        try {
            yaml.save(new File(dataFolder, uuid + ".yml"));
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to save homes for " + uuid, ex);
        }
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) {
            saveHomes(uuid);
        }
    }

    private Map<String, Location> loadHomes(UUID uuid) {
        Map<String, Location> homes = new LinkedHashMap<>();
        File file = new File(dataFolder, uuid + ".yml");
        if (!file.exists()) {
            return homes;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("homes");
        if (section == null) {
            return homes;
        }

        for (String name : section.getKeys(false)) {
            ConfigurationSection homeSection = section.getConfigurationSection(name);
            Location loc = deserialize(homeSection);
            if (loc != null) {
                homes.put(name, loc);
            }
        }
        return homes;
    }

    private Location deserialize(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        World world = plugin.getServer().getWorld(section.getString("world", ""));
        if (world == null) {
            return null;
        }
        return new Location(world, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"),
                (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
    }
}
