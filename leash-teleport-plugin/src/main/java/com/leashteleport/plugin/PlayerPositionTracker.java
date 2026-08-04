package com.leashteleport.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerPositionTracker {

    private record Sample(long time, Location location) {
    }

    private static final long SAMPLE_INTERVAL_TICKS = 10L; // 0.5s
    private static final long RETENTION_MS = 12_000L;

    private final LeashTeleportPlugin plugin;
    private final Map<UUID, Deque<Sample>> history = new HashMap<>();
    private BukkitTask task;

    public PlayerPositionTracker(LeashTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::sampleAll, SAMPLE_INTERVAL_TICKS, SAMPLE_INTERVAL_TICKS);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        history.clear();
    }

    public void forget(UUID playerId) {
        history.remove(playerId);
    }

    private void sampleAll() {
        long now = System.currentTimeMillis();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Deque<Sample> deque = history.computeIfAbsent(player.getUniqueId(), id -> new ArrayDeque<>());
            deque.addLast(new Sample(now, player.getLocation()));
            while (!deque.isEmpty() && now - deque.peekFirst().time() > RETENTION_MS) {
                deque.removeFirst();
            }
        }
    }

    /**
     * Returns the closest recorded location at least {@code agoMillis} old,
     * or {@code null} if there isn't enough history yet.
     */
    public Location getLocationAround(UUID playerId, long agoMillis) {
        Deque<Sample> deque = history.get(playerId);
        if (deque == null || deque.isEmpty()) {
            return null;
        }

        long targetTime = System.currentTimeMillis() - agoMillis;
        Location best = null;
        for (Sample sample : deque) {
            if (sample.time() <= targetTime) {
                best = sample.location();
            } else {
                break;
            }
        }

        if (best != null) {
            return best;
        }

        Sample oldest = deque.peekFirst();
        if (oldest != null && System.currentTimeMillis() - oldest.time() >= 1000L) {
            return oldest.location();
        }
        return null;
    }
}
