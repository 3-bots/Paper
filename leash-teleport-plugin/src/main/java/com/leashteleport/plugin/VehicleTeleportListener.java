package com.leashteleport.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * By default, teleporting a player who is riding something (a horse, boat,
 * etc.) dismounts them first and leaves the vehicle behind. This brings the
 * vehicle - and anything else riding it - along to the destination instead,
 * by cancelling the player's own teleport and teleporting the vehicle
 * (which carries its passengers with it).
 */
public final class VehicleTeleportListener implements Listener {

    private final LeashTeleportPlugin plugin;

    public VehicleTeleportListener(LeashTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            return;
        }

        Location destination = event.getTo();
        if (destination == null || destination.getWorld() == null) {
            return;
        }

        event.setCancelled(true);

        Location dest = destination.clone();
        dest.getChunk(); // force-load so long-distance teleports don't silently fail

        vehicle.teleport(dest);

        if (plugin.isNotifyEnabled()) {
            player.sendMessage(plugin.message("brought-mount"));
        }
    }
}
