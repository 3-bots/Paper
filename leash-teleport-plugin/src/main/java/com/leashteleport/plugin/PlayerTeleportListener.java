package com.leashteleport.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PlayerTeleportListener implements Listener {

    private final LeashTeleportPlugin plugin;

    public PlayerTeleportListener(LeashTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || to.getWorld() == null || from.getWorld() == null) {
            return;
        }

        // Move the leashed entities right now, synchronously, while the
        // player's own teleport is still pending (Paper fires this event
        // before actually relocating the player). Waiting even one tick is
        // too late: vanilla's leash-distance check runs on the next tick
        // and snaps the lead once the player is far away but the pet isn't.
        bringLeashedEntities(player, from.clone(), to.clone());
    }

    private void bringLeashedEntities(Player player, Location origin, Location destination) {
        double radius = plugin.getSearchRadius();
        Collection<Entity> nearby = origin.getWorld().getNearbyEntities(origin, radius, radius, radius);

        List<LivingEntity> leashedToPlayer = new ArrayList<>();
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity living && living.isLeashed()) {
                try {
                    if (player.equals(living.getLeashHolder())) {
                        leashedToPlayer.add(living);
                    }
                } catch (IllegalStateException ignored) {
                    // Not actually leashed anymore by the time we asked; skip it.
                }
            }
        }

        if (leashedToPlayer.isEmpty()) {
            return;
        }

        Set<Entity> handledVehicles = new HashSet<>();
        int index = 0;
        int moved = 0;

        for (LivingEntity living : leashedToPlayer) {
            Entity vehicle = living.getVehicle();
            Location dest = offset(destination, index++, plugin.getSpreadRadius());

            // Regular (non-player) entities don't reliably force-load their
            // destination chunk the way a player teleport does. Over long
            // distances the target chunk is often never loaded, and
            // Entity#teleport() then just silently returns false. Force the
            // chunk to load first so the teleport actually succeeds.
            dest.getChunk();

            if (vehicle != null) {
                if (!handledVehicles.add(vehicle)) {
                    continue;
                }
                List<Entity> passengers = new ArrayList<>(vehicle.getPassengers());
                for (Entity passenger : passengers) {
                    passenger.leaveVehicle();
                }
                if (!vehicle.teleport(dest)) {
                    plugin.getLogger().warning("Failed to teleport vehicle " + vehicle.getType() + " for " + player.getName());
                }
                for (Entity passenger : passengers) {
                    if (!passenger.teleport(dest)) {
                        plugin.getLogger().warning("Failed to teleport passenger " + passenger.getType() + " for " + player.getName());
                    }
                    vehicle.addPassenger(passenger);
                    if (passenger instanceof LivingEntity passengerLiving && leashedToPlayer.contains(passengerLiving)) {
                        passengerLiving.setLeashHolder(player);
                    }
                }
            } else {
                if (!living.teleport(dest)) {
                    plugin.getLogger().warning("Failed to teleport " + living.getType() + " for " + player.getName());
                }
                living.setLeashHolder(player);
            }
            moved++;
        }

        if (moved > 0 && plugin.isNotifyEnabled()) {
            player.sendMessage(plugin.message("brought-along", moved));
        }
    }

    private Location offset(Location base, int index, double spread) {
        if (index == 0) {
            return base.clone();
        }
        double angle = index * (Math.PI / 3);
        double dx = Math.cos(angle) * spread;
        double dz = Math.sin(angle) * spread;
        return base.clone().add(dx, 0, dz);
    }
}
