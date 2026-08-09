package com.villagerinstantjob.plugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Map;

public final class JobBlockPlaceListener implements Listener {

    private static final Map<Material, Villager.Profession> PROFESSION_BY_BLOCK = Map.ofEntries(
            Map.entry(Material.BLAST_FURNACE, Villager.Profession.ARMORER),
            Map.entry(Material.SMOKER, Villager.Profession.BUTCHER),
            Map.entry(Material.CARTOGRAPHY_TABLE, Villager.Profession.CARTOGRAPHER),
            Map.entry(Material.BREWING_STAND, Villager.Profession.CLERIC),
            Map.entry(Material.COMPOSTER, Villager.Profession.FARMER),
            Map.entry(Material.BARREL, Villager.Profession.FISHERMAN),
            Map.entry(Material.FLETCHING_TABLE, Villager.Profession.FLETCHER),
            Map.entry(Material.CAULDRON, Villager.Profession.LEATHERWORKER),
            Map.entry(Material.LECTERN, Villager.Profession.LIBRARIAN),
            Map.entry(Material.STONECUTTER, Villager.Profession.MASON),
            Map.entry(Material.LOOM, Villager.Profession.SHEPHERD),
            Map.entry(Material.SMITHING_TABLE, Villager.Profession.TOOLSMITH),
            Map.entry(Material.GRINDSTONE, Villager.Profession.WEAPONSMITH)
    );

    private final VillagerInstantJobPlugin plugin;

    public JobBlockPlaceListener(VillagerInstantJobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Villager.Profession profession = PROFESSION_BY_BLOCK.get(event.getBlock().getType());
        if (profession == null) {
            return;
        }

        Block block = event.getBlock();
        double radius = plugin.getSearchRadius();

        Villager closest = null;
        double closestDistanceSquared = Double.MAX_VALUE;

        for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), radius, radius, radius)) {
            if (!(entity instanceof Villager villager)) {
                continue;
            }
            if (!villager.isAdult()) {
                continue;
            }
            if (villager.getProfession() != Villager.Profession.NONE) {
                continue;
            }
            if (villager.getMemory(MemoryKey.JOB_SITE) != null) {
                continue;
            }

            double distanceSquared = villager.getLocation().distanceSquared(block.getLocation());
            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closest = villager;
            }
        }

        if (closest == null) {
            return;
        }

        closest.setProfession(profession);
        closest.setMemory(MemoryKey.JOB_SITE, block.getLocation());
    }
}
