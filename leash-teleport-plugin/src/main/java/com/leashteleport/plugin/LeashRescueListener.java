package com.leashteleport.plugin;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class LeashRescueListener implements Listener {

    private static final double SCAN_RADIUS = 30.0;

    private final LeashTeleportPlugin plugin;
    private final PlayerPositionTracker tracker;
    private final Map<UUID, UUID> leashOwners = new HashMap<>();
    private BukkitTask scanTask;

    public LeashRescueListener(LeashTeleportPlugin plugin, PlayerPositionTracker tracker) {
        this.plugin = plugin;
        this.tracker = tracker;
    }

    /**
     * PlayerLeashEntityEvent only tells us about leashes formed after this
     * plugin started. Anything leashed earlier (a previous session, before
     * a plugin update/restart) would never be in {@link #leashOwners} and
     * would silently fail to be rescued. This periodically reconciles the
     * map against actual entity leash state so it self-heals regardless of
     * when the leash was formed.
     */
    public void start() {
        scanTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::scanForLeashedEntities, 0L, 20L);
    }

    public void stop() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
    }

    private void scanForLeashedEntities() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            for (Entity entity : player.getNearbyEntities(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS)) {
                if (entity instanceof LivingEntity living && living.isLeashed()) {
                    try {
                        if (player.equals(living.getLeashHolder())) {
                            leashOwners.put(living.getUniqueId(), player.getUniqueId());
                        }
                    } catch (IllegalStateException ignored) {
                        // Not actually leashed anymore by the time we asked; skip it.
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeash(PlayerLeashEntityEvent event) {
        leashOwners.put(event.getEntity().getUniqueId(), event.getPlayer().getUniqueId());
    }

    /**
     * Vanilla Minecraft does not allow leashing villagers at all (which is
     * why boats/minecarts are the usual way to move them) - right-clicking
     * one with a lead just opens trades and no PlayerLeashEntityEvent ever
     * fires. This adds that support: holding a lead and right-clicking an
     * unleashed villager leashes it instead of opening trades.
     */
    @EventHandler(ignoreCancelled = true)
    public void onInteractVillager(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!(event.getRightClicked() instanceof Villager villager) || villager.isLeashed()) {
            return;
        }

        Player player = event.getPlayer();
        EquipmentSlot leadSlot = null;
        if (player.getInventory().getItemInMainHand().getType() == Material.LEAD) {
            leadSlot = EquipmentSlot.HAND;
        } else if (player.getInventory().getItemInOffHand().getType() == Material.LEAD) {
            leadSlot = EquipmentSlot.OFF_HAND;
        }
        if (leadSlot == null) {
            return;
        }

        event.setCancelled(true);
        villager.setLeashHolder(player);
        leashOwners.put(villager.getUniqueId(), player.getUniqueId());

        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack lead = leadSlot == EquipmentSlot.HAND
                    ? player.getInventory().getItemInMainHand()
                    : player.getInventory().getItemInOffHand();
            lead.setAmount(lead.getAmount() - 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUnleash(EntityUnleashEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }

        UUID entityId = living.getUniqueId();
        UUID holderId = leashOwners.get(entityId);
        if (holderId == null) {
            return;
        }

        if (event.getReason() != EntityUnleashEvent.UnleashReason.DISTANCE) {
            leashOwners.remove(entityId);
            return;
        }

        Player holder = plugin.getServer().getPlayer(holderId);
        if (holder == null) {
            leashOwners.remove(entityId);
            return;
        }

        long windowMillis = plugin.getDetectWindowSeconds() * 1000L;
        Location past = tracker.getLocationAround(holderId, windowMillis);
        Location current = holder.getLocation();

        boolean jumped;
        if (past == null) {
            jumped = false;
        } else if (!Objects.equals(past.getWorld(), current.getWorld())) {
            jumped = true;
        } else {
            jumped = past.distance(current) > plugin.getTeleportDetectBlocks();
        }

        if (!jumped) {
            leashOwners.remove(entityId);
            return;
        }

        rescue(living, holder);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        tracker.forget(event.getPlayer().getUniqueId());
    }

    private void rescue(LivingEntity living, Player holder) {
        Location dest = offset(holder.getLocation(), living.getEntityId());
        dest.getChunk();

        Entity vehicle = living.getVehicle();
        if (vehicle != null) {
            List<Entity> passengers = new ArrayList<>(vehicle.getPassengers());
            for (Entity passenger : passengers) {
                passenger.leaveVehicle();
            }
            vehicle.teleport(dest);
            for (Entity passenger : passengers) {
                passenger.teleport(dest);
                vehicle.addPassenger(passenger);
            }
        } else {
            living.teleport(dest);
        }

        living.setLeashHolder(holder);

        if (plugin.isNotifyEnabled()) {
            holder.sendMessage(plugin.message("brought-along", 1));
        }
    }

    private Location offset(Location base, int entityId) {
        double angle = (entityId % 6) * (Math.PI / 3);
        double spread = plugin.getSpreadRadius();
        return base.clone().add(Math.cos(angle) * spread, 0, Math.sin(angle) * spread);
    }
}
