package com.villagerinstantjob.plugin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

import java.util.UUID;

/**
 * Vanilla runs a "POI competitor scan" on every villager every tick that can
 * strip a JOB_SITE memory outright with no Bukkit event fired at all - since
 * our claim isn't a real POI reservation, any other idle villager nearby can
 * legitimately out-compete it for the same workstation through completely
 * normal vanilla behavior. There's no event to intercept for that, so this
 * runs every tick too, restoring any tracked assignment the instant it's
 * knocked out instead of leaving it dropped for up to a second.
 */
public final class JobPersistenceTask implements Runnable {

    private final VillagerInstantJobPlugin plugin;

    public JobPersistenceTask(VillagerInstantJobPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (UUID id : JobClaimer.trackedVillagerIds()) {
            Entity entity = plugin.getServer().getEntity(id);
            if (entity instanceof Villager villager) {
                JobClaimer.enforceAssignment(villager);
            }
        }
    }
}
