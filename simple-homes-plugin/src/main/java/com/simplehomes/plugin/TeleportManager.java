package com.simplehomes.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class TeleportManager {

    private final SimpleHomesPlugin plugin;
    private final Map<UUID, BukkitTask> pending = new HashMap<>();

    public TeleportManager(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isPending(UUID uuid) {
        return pending.containsKey(uuid);
    }

    public void startTeleport(Player player, Location target) {
        UUID uuid = player.getUniqueId();
        if (pending.containsKey(uuid)) {
            player.sendMessage(plugin.message("teleporting_already"));
            return;
        }

        int delay = plugin.getTeleportDelaySeconds();
        if (delay <= 0) {
            player.teleport(target);
            player.sendMessage(plugin.message("teleported"));
            return;
        }

        player.sendMessage(plugin.message("teleporting"));
        AtomicInteger secondsLeft = new AtomicInteger(delay);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int seconds = secondsLeft.getAndDecrement();
            if (seconds <= 0) {
                cancelTeleport(uuid, false);
                player.teleport(target);
                player.sendMessage(plugin.message("teleported"));
                return;
            }
            player.sendMessage(plugin.message("teleporting_countdown", seconds));
        }, 0L, 20L);

        pending.put(uuid, task);
    }

    public void cancelTeleport(UUID uuid, boolean notify) {
        BukkitTask task = pending.remove(uuid);
        if (task == null) {
            return;
        }
        task.cancel();
        if (notify) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(plugin.message("teleport_cancelled_move"));
            }
        }
    }

    public void cancelAll() {
        for (BukkitTask task : pending.values()) {
            task.cancel();
        }
        pending.clear();
    }
}
