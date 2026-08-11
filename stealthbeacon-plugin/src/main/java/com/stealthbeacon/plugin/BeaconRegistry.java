package com.stealthbeacon.plugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class BeaconRegistry {

    public static final int MAX_TIER = 4;

    public record Beacon(Location location, int tier) {
    }

    private final StealthBeaconPlugin plugin;
    private final File file;
    private final List<Location> markers = new ArrayList<>();
    private final List<Beacon> activeBeacons = new ArrayList<>();

    public BeaconRegistry(StealthBeaconPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "beacons.yml");
        load();
    }

    public void register(Location markerLocation) {
        markers.add(markerLocation.getBlock().getLocation());
        save();
        revalidate();
    }

    public void unregister(Location markerLocation) {
        Location target = markerLocation.getBlock().getLocation();
        markers.removeIf(loc -> sameBlock(loc, target));
        save();
        revalidate();
    }

    public List<Beacon> getActiveBeacons() {
        return activeBeacons;
    }

    /** Re-checks every registered marker's pyramid and rebuilds the active-beacon list. */
    public void revalidate() {
        activeBeacons.clear();
        for (Location marker : markers) {
            int tier = computeTier(marker);
            if (tier > 0) {
                activeBeacons.add(new Beacon(marker, tier));
            }
        }
    }

    /**
     * Counts complete diamond-block rings stacked directly beneath the marker,
     * same layout as a vanilla beacon pyramid (3x3, 5x5, 7x7, 9x9), stopping at
     * the first incomplete or missing ring.
     */
    public int computeTier(Location marker) {
        World world = marker.getWorld();
        if (world == null) {
            return 0;
        }
        int mx = marker.getBlockX();
        int my = marker.getBlockY();
        int mz = marker.getBlockZ();

        int tier = 0;
        for (int t = 1; t <= MAX_TIER; t++) {
            if (!isFullDiamondRing(world, mx, my - t, mz, t)) {
                break;
            }
            tier = t;
        }
        return tier;
    }

    private boolean isFullDiamondRing(World world, int mx, int y, int mz, int t) {
        for (int dx = -t; dx <= t; dx++) {
            for (int dz = -t; dz <= t; dz++) {
                if (world.getBlockAt(mx + dx, y, mz + dz).getType() != Material.DIAMOND_BLOCK) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean sameBlock(Location a, Location b) {
        return a.getWorld() != null && a.getWorld().equals(b.getWorld())
                && a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<?> list = yaml.getList("markers");
        if (list == null) {
            return;
        }
        for (Object obj : list) {
            if (obj instanceof Location loc && loc.getWorld() != null) {
                markers.add(loc);
            }
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("markers", markers);
        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to save beacon markers", ex);
        }
    }
}
