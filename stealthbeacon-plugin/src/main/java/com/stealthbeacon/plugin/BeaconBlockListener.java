package com.stealthbeacon.plugin;

import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class BeaconBlockListener implements Listener {

    private final StealthBeaconPlugin plugin;

    public BeaconBlockListener(StealthBeaconPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != plugin.getMarkerMaterial()) {
            return;
        }
        plugin.getBeaconRegistry().register(event.getBlock().getLocation());
        event.getPlayer().sendMessage(plugin.message("beacon_placed"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != plugin.getMarkerMaterial()) {
            return;
        }
        plugin.getBeaconRegistry().unregister(event.getBlock().getLocation());
        event.getPlayer().sendMessage(plugin.message("beacon_removed"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        if (event.getClickedBlock().getType() != plugin.getMarkerMaterial()) {
            return;
        }

        int tier = plugin.getBeaconRegistry().computeTier(event.getClickedBlock().getLocation());
        if (tier <= 0) {
            event.getPlayer().sendMessage(plugin.message("beacon_inactive"));
            return;
        }

        int horizontal = (int) plugin.getHorizontalRadius(tier);
        int vertical = (int) plugin.getVerticalRadius(tier);
        event.getPlayer().sendMessage(plugin.message("beacon_info", tier, horizontal, vertical));
    }
}
