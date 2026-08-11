package com.hardcorerevival.plugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.concurrent.ThreadLocalRandom;

/**
 * When a revival completes, the ritual takes its toll on the altar: each
 * iron/gold/diamond block nearby has an independent random chance of being
 * struck by lightning and destroyed.
 */
public final class AltarConsumption {

    private final HardcoreRevivalPlugin plugin;

    public AltarConsumption(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    /** Returns how many blocks were consumed. */
    public int strikeAndConsume(Location fire) {
        World world = fire.getWorld();
        if (world == null) {
            return 0;
        }

        double chance = plugin.getRitualBlockLossChance();
        if (chance <= 0) {
            return 0;
        }

        int radius = plugin.getAltarRadius();
        int fx = fire.getBlockX();
        int fy = fire.getBlockY();
        int fz = fire.getBlockZ();
        int consumed = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block block = world.getBlockAt(fx + dx, fy + dy, fz + dz);
                    Material type = block.getType();
                    if (type != Material.IRON_BLOCK && type != Material.GOLD_BLOCK && type != Material.DIAMOND_BLOCK) {
                        continue;
                    }
                    if (ThreadLocalRandom.current().nextDouble() >= chance) {
                        continue;
                    }

                    world.strikeLightningEffect(block.getLocation().add(0.5, 0.5, 0.5));
                    block.setType(Material.AIR);
                    consumed++;
                }
            }
        }

        return consumed;
    }
}
