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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JobClaimer {

    /** Ticks after the initial claim to reassert it, in case vanilla's own AI reverts it. */
    private static final long[] REASSERT_DELAYS_TICKS = { 1L, 5L, 20L, 40L };

    /**
     * Every villager this plugin has ever assigned a job to, tracked so a
     * recurring watchdog (see JobPersistenceTask) can restore the assignment
     * no matter when vanilla's AI decides to drop it - not just in the few
     * seconds right after the claim. Entries are removed once the job site
     * block stops being a matching workstation (broken, replaced, etc.).
     */
    private static final Map<UUID, Assignment> ACTIVE_ASSIGNMENTS = new ConcurrentHashMap<>();

    private record Assignment(Villager.Profession profession, Location jobSite) {
    }

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
        ACTIVE_ASSIGNMENTS.put(villager.getUniqueId(), new Assignment(profession, jobSite));

        Location particleLocation = villager.getLocation().add(0, 1, 0);
        villager.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, particleLocation, 15, 0.4, 0.5, 0.4, 0.0);
    }

    public static int activeAssignmentCount() {
        return ACTIVE_ASSIGNMENTS.size();
    }

    /**
     * Called on a recurring watchdog for every villager. Vanilla's AI can
     * re-validate a job site at any time - not just right after the claim -
     * and silently clear it because our claim was never registered with its
     * internal POI system. This restores the assignment whenever that
     * happens, for as long as the workstation block is still there.
     */
    public static void enforceAssignment(Villager villager) {
        Assignment assignment = ACTIVE_ASSIGNMENTS.get(villager.getUniqueId());
        if (assignment == null) {
            return;
        }
        if (!villager.isValid()) {
            ACTIVE_ASSIGNMENTS.remove(villager.getUniqueId());
            return;
        }

        Block jobBlock = assignment.jobSite().getBlock();
        if (PROFESSION_BY_BLOCK.get(jobBlock.getType()) != assignment.profession()) {
            ACTIVE_ASSIGNMENTS.remove(villager.getUniqueId());
            return;
        }

        if (villager.getProfession() != assignment.profession()
                || !assignment.jobSite().equals(villager.getMemory(MemoryKey.JOB_SITE))) {
            applyAssignment(villager, assignment.profession(), assignment.jobSite());
        }
    }
}
