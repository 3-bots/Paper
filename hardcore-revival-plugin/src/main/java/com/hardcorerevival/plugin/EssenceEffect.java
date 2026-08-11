package com.hardcorerevival.plugin;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

/** Streams particles from the sacrificed villager's death spot into the ritual fire. */
public final class EssenceEffect {

    private final HardcoreRevivalPlugin plugin;

    public EssenceEffect(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    public void playEssenceStream(Location from, Location to) {
        World world = from.getWorld();
        if (world == null || !world.equals(to.getWorld())) {
            return;
        }

        int steps = 20;
        double dx = (to.getX() - from.getX()) / steps;
        double dy = (to.getY() - from.getY()) / steps;
        double dz = (to.getZ() - from.getZ()) / steps;

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step > steps) {
                    cancel();
                    return;
                }
                Location point = from.clone().add(dx * step, dy * step, dz * step);
                world.spawnParticle(Particle.SOUL, point, 3, 0.1, 0.1, 0.1, 0.01);
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
