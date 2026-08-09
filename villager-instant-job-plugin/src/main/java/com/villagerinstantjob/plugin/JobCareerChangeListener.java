package com.villagerinstantjob.plugin;

import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;

/**
 * Vanilla fires this event before it changes a villager's profession,
 * including when its brain decides to demote a villager back to unemployed
 * because it doesn't recognize this plugin's job-site claim. Cancelling it
 * for a villager this plugin has assigned stops that flip from ever
 * happening, instead of only correcting it a moment later.
 */
public final class JobCareerChangeListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onCareerChange(VillagerCareerChangeEvent event) {
        Villager villager = event.getEntity();
        if (JobClaimer.shouldPreventChange(villager, event.getProfession())) {
            event.setCancelled(true);
        }
    }
}
