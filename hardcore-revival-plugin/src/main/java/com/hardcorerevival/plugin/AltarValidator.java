package com.hardcorerevival.plugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Checks whether the area around a fire block has everything a ritual altar
 * needs: enough iron blocks, enough diamond blocks, a torch on top of a gold
 * block ("gold torch"), and a redstone torch, all within settings.altar-radius.
 */
public final class AltarValidator {

    private final HardcoreRevivalPlugin plugin;

    public AltarValidator(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isValidAltar(Location fireLocation) {
        World world = fireLocation.getWorld();
        if (world == null) {
            return false;
        }

        int radius = plugin.getAltarRadius();
        int ironCount = 0;
        int diamondCount = 0;
        boolean hasGoldTorch = false;
        boolean hasRedstoneTorch = false;

        int fx = fireLocation.getBlockX();
        int fy = fireLocation.getBlockY();
        int fz = fireLocation.getBlockZ();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block block = world.getBlockAt(fx + dx, fy + dy, fz + dz);
                    Material type = block.getType();

                    if (type == Material.IRON_BLOCK) {
                        ironCount++;
                    } else if (type == Material.DIAMOND_BLOCK) {
                        diamondCount++;
                    } else if (type == Material.REDSTONE_TORCH || type == Material.REDSTONE_WALL_TORCH) {
                        hasRedstoneTorch = true;
                    } else if (type == Material.TORCH || type == Material.WALL_TORCH) {
                        if (block.getRelative(0, -1, 0).getType() == Material.GOLD_BLOCK) {
                            hasGoldTorch = true;
                        }
                    }
                }
            }
        }

        return ironCount >= plugin.getRequiredIronBlocks()
                && diamondCount >= plugin.getRequiredDiamondBlocks()
                && hasGoldTorch
                && hasRedstoneTorch;
    }
}
