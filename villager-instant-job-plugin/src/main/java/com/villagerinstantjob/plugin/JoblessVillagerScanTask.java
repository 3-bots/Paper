package com.villagerinstantjob.plugin;

import org.bukkit.World;
import org.bukkit.entity.Villager;

public final class JoblessVillagerScanTask implements Runnable {

    private final VillagerInstantJobPlugin plugin;

    public JoblessVillagerScanTask(VillagerInstantJobPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int claimedCount = 0;
        for (World world : plugin.getServer().getWorlds()) {
            for (Villager villager : world.getEntitiesByClass(Villager.class)) {
                if (JobClaimer.claimForVillager(villager, plugin.getScanBlockRadius())) {
                    claimedCount++;
                }
            }
        }
        if (claimedCount > 0) {
            plugin.getLogger().info("Sweep claimed jobs for " + claimedCount + " previously-jobless villager(s).");
        }
    }
}
