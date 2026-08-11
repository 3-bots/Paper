package com.hardcorerevival.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public final class VillagerSacrificeListener implements Listener {

    private final HardcoreRevivalPlugin plugin;

    public VillagerSacrificeListener(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        Player killer = villager.getKiller();
        if (killer == null) {
            return;
        }

        Location fire = plugin.getAltarFinder().findNearbyAltarFire(villager.getLocation());
        if (fire == null) {
            return;
        }

        plugin.getRitualState().markSacrificed(AltarKeys.of(fire));
        killer.sendMessage(plugin.message("sacrifice_done"));
        plugin.getEssenceEffect().playEssenceStream(villager.getLocation().add(0, 1, 0), fire.clone().add(0.5, 0.5, 0.5));
    }
}
