package com.hardcorerevival.plugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class AltarFinder {

    private final HardcoreRevivalPlugin plugin;
    private final AltarValidator validator;

    public AltarFinder(HardcoreRevivalPlugin plugin, AltarValidator validator) {
        this.plugin = plugin;
        this.validator = validator;
    }

    /** Looks for a valid altar's fire block within altar-radius of the given location. */
    public Location findNearbyAltarFire(Location origin) {
        World world = origin.getWorld();
        if (world == null) {
            return null;
        }

        int radius = plugin.getAltarRadius();
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block block = world.getBlockAt(ox + dx, oy + dy, oz + dz);
                    if (block.getType() == plugin.getFireMaterial() && validator.isValidAltar(block.getLocation())) {
                        return block.getLocation();
                    }
                }
            }
        }
        return null;
    }
}
