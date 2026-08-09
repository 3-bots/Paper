package com.villagerinstantjob.plugin;

import org.bukkit.World;
import org.bukkit.entity.Villager;

/**
 * Continuously re-checks every villager this plugin has assigned a job to,
 * restoring the assignment if vanilla's AI drops it later on - not just in
 * the few seconds right after the original claim.
 */
public final class JobPersistenceTask implements Runnable {

    private final VillagerInstantJobPlugin plugin;

    public JobPersistenceTask(VillagerInstantJobPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (JobClaimer.activeAssignmentCount() == 0) {
            return;
        }
        for (World world : plugin.getServer().getWorlds()) {
            for (Villager villager : world.getEntitiesByClass(Villager.class)) {
                JobClaimer.enforceAssignment(villager);
            }
        }
    }
}
