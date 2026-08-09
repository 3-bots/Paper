package com.villagerinstantjob.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public final class JobBlockPlaceListener implements Listener {

    private final VillagerInstantJobPlugin plugin;

    public JobBlockPlaceListener(VillagerInstantJobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        boolean claimed = JobClaimer.claimForBlock(plugin, event.getBlock(), plugin.getSearchRadius());
        if (claimed) {
            plugin.getLogger().info("Villager claimed " + event.getBlock().getType() + " at "
                    + formatLocation(event.getBlock().getLocation()) + " as a job site (on place).");
        }
    }

    static String formatLocation(org.bukkit.Location location) {
        return location.getWorld().getName() + " " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
}
