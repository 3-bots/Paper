package com.simplehomes.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TpaManager {

    public record PendingRequest(UUID requesterId, boolean here) {
    }

    private final SimpleHomesPlugin plugin;
    private final Map<UUID, PendingRequest> pendingByTarget = new HashMap<>();
    private final Map<UUID, BukkitTask> expiryTasks = new HashMap<>();

    public TpaManager(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
    }

    /** Records a request from requester to target. "here" means the target is being asked to come to the requester. */
    public void request(Player requester, Player target, boolean here) {
        UUID targetId = target.getUniqueId();
        cancelExpiry(targetId);
        pendingByTarget.put(targetId, new PendingRequest(requester.getUniqueId(), here));

        int expirySeconds = plugin.getTpaExpirySeconds();
        if (expirySeconds <= 0) {
            return;
        }

        UUID requesterId = requester.getUniqueId();
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PendingRequest current = pendingByTarget.get(targetId);
            if (current == null || !current.requesterId().equals(requesterId)) {
                return;
            }
            pendingByTarget.remove(targetId);
            expiryTasks.remove(targetId);

            Player targetOnline = Bukkit.getPlayer(targetId);
            if (targetOnline != null) {
                targetOnline.sendMessage(plugin.message("tpa_expired_target", requester.getName()));
            }
            Player requesterOnline = Bukkit.getPlayer(requesterId);
            if (requesterOnline != null) {
                requesterOnline.sendMessage(plugin.message("tpa_expired_requester", target.getName()));
            }
        }, expirySeconds * 20L);
        expiryTasks.put(targetId, task);
    }

    public PendingRequest getPending(UUID targetId) {
        return pendingByTarget.get(targetId);
    }

    public void clear(UUID targetId) {
        pendingByTarget.remove(targetId);
        cancelExpiry(targetId);
    }

    /** Clears any request where this player is either the target or the requester. */
    public void clearInvolving(UUID uuid) {
        clear(uuid);
        pendingByTarget.entrySet().stream()
                .filter(entry -> entry.getValue().requesterId().equals(uuid))
                .map(Map.Entry::getKey)
                .toList()
                .forEach(this::clear);
    }

    public void cancelAll() {
        for (BukkitTask task : expiryTasks.values()) {
            task.cancel();
        }
        expiryTasks.clear();
        pendingByTarget.clear();
    }

    private void cancelExpiry(UUID targetId) {
        BukkitTask task = expiryTasks.remove(targetId);
        if (task != null) {
            task.cancel();
        }
    }
}
