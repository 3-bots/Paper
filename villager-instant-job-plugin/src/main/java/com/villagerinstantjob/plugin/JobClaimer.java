package com.villagerinstantjob.plugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;

import java.util.Map;

public final class JobClaimer {

    /** Ticks after the initial claim to reassert it, in case vanilla's own AI reverts it. */
    private static final long[] REASSERT_DELAYS_TICKS = { 1L, 5L, 20L, 40L };

    public static final Map<Material, Villager.Profession> PROFESSION_BY_BLOCK = Map.ofEntries(
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

    private JobClaimer() {
    }

    public static boolean isJobless(Villager villager) {
        return villager.isAdult()
                && villager.getProfession() == Villager.Profession.NONE
                && villager.getMemory(MemoryKey.JOB_SITE) == null;
    }

    /** Finds the nearest jobless villager around a specific block and assigns it that job. */
    public static boolean claimForBlock(VillagerInstantJobPlugin plugin, Block block, double entityRadius) {
        Villager.Profession profession = PROFESSION_BY_BLOCK.get(block.getType());
        if (profession == null) {
            return false;
        }

        Villager closest = null;
        double closestDistanceSquared = Double.MAX_VALUE;

        for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), entityRadius, entityRadius, entityRadius)) {
            if (!(entity instanceof Villager villager) || !isJobless(villager)) {
                continue;
            }
            double distanceSquared = villager.getLocation().distanceSquared(block.getLocation());
            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closest = villager;
            }
        }

        if (closest == null) {
            return false;
        }

        assign(plugin, closest, profession, block.getLocation());
        return true;
    }

    /** Finds the nearest unclaimed matching workstation block around a jobless villager and assigns it. */
    public static boolean claimForVillager(VillagerInstantJobPlugin plugin, Villager villager, int blockRadius) {
        if (!isJobless(villager)) {
            return false;
        }

        World world = villager.getWorld();
        Location center = villager.getLocation();
        Block originBlock = center.getBlock();

        Block bestBlock = null;
        Villager.Profession bestProfession = null;
        double bestDistanceSquared = Double.MAX_VALUE;

        for (int dx = -blockRadius; dx <= blockRadius; dx++) {
            for (int dy = -blockRadius; dy <= blockRadius; dy++) {
                for (int dz = -blockRadius; dz <= blockRadius; dz++) {
                    Block block = originBlock.getRelative(dx, dy, dz);
                    Villager.Profession profession = PROFESSION_BY_BLOCK.get(block.getType());
                    if (profession == null) {
                        continue;
                    }
                    if (isClaimedByAnother(world, block.getLocation(), villager)) {
                        continue;
                    }
                    double distanceSquared = center.distanceSquared(block.getLocation());
                    if (distanceSquared < bestDistanceSquared) {
                        bestDistanceSquared = distanceSquared;
                        bestBlock = block;
                        bestProfession = profession;
                    }
                }
            }
        }

        if (bestBlock == null) {
            return false;
        }

        assign(plugin, villager, bestProfession, bestBlock.getLocation());
        return true;
    }

    private static boolean isClaimedByAnother(World world, Location jobSite, Villager exclude) {
        for (Entity entity : world.getNearbyEntities(jobSite, 3, 3, 3)) {
            if (!(entity instanceof Villager other) || other.equals(exclude)) {
                continue;
            }
            Location otherSite = other.getMemory(MemoryKey.JOB_SITE);
            if (otherSite != null && otherSite.getWorld() != null && otherSite.getWorld().equals(jobSite.getWorld())
                    && otherSite.getBlockX() == jobSite.getBlockX()
                    && otherSite.getBlockY() == jobSite.getBlockY()
                    && otherSite.getBlockZ() == jobSite.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Setting profession + JOB_SITE memory through the API doesn't register the
     * claim with vanilla's own internal point-of-interest reservation system.
     * That mismatch means the villager's own AI can notice on a later tick that
     * it's not actually holding a real claim on that POI and silently clear the
     * memory (and sometimes the profession) again a moment later. Reasserting
     * the assignment a few times over the following couple seconds wins that
     * race instead of losing to it.
     */
    private static void assign(VillagerInstantJobPlugin plugin, Villager villager, Villager.Profession profession, Location jobSite) {
        applyAssignment(villager, profession, jobSite);

        for (long delay : REASSERT_DELAYS_TICKS) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!villager.isValid()) {
                    return;
                }
                if (villager.getProfession() != profession || !jobSite.equals(villager.getMemory(MemoryKey.JOB_SITE))) {
                    applyAssignment(villager, profession, jobSite);
                }
            }, delay);
        }
    }

    private static void applyAssignment(Villager villager, Villager.Profession profession, Location jobSite) {
        villager.setProfession(profession);
        villager.setMemory(MemoryKey.JOB_SITE, jobSite);

        Location particleLocation = villager.getLocation().add(0, 1, 0);
        villager.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, particleLocation, 15, 0.4, 0.5, 0.4, 0.0);
    }
}
