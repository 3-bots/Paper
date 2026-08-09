package com.villagerworkstation.plugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;

public final class WorkstationSnapTask implements Runnable {

    private static final BlockFace[] SIDES = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

    private final VillagerWorkstationPlugin plugin;

    public WorkstationSnapTask(VillagerWorkstationPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (World world : plugin.getServer().getWorlds()) {
            if (plugin.isOnlyDaytime() && !world.isDayTime()) {
                continue;
            }

            for (Villager villager : world.getEntitiesByClass(Villager.class)) {
                snapIfNeeded(villager);
            }
        }
    }

    private void snapIfNeeded(Villager villager) {
        Location jobSite = villager.getMemory(MemoryKey.JOB_SITE);
        if (jobSite == null || jobSite.getWorld() == null || !jobSite.getWorld().equals(villager.getWorld())) {
            return;
        }

        double distance = villager.getLocation().distance(jobSite);
        if (distance <= plugin.getMaxDistance()) {
            return;
        }

        Location safe = findSafeSpot(jobSite.getBlock());
        if (safe == null) {
            return;
        }

        safe.setYaw(villager.getLocation().getYaw());
        safe.setPitch(0f);
        safe.getChunk();
        villager.teleport(safe);
    }

    private Location findSafeSpot(Block jobBlock) {
        for (BlockFace face : SIDES) {
            Block feet = jobBlock.getRelative(face);
            if (isStandable(feet)) {
                return feet.getLocation().add(0.5, 0, 0.5);
            }
        }

        Block above = jobBlock.getRelative(BlockFace.UP);
        if (isStandable(above)) {
            return above.getLocation().add(0.5, 0, 0.5);
        }

        return null;
    }

    private boolean isStandable(Block feet) {
        Material feetType = feet.getType();
        Material headType = feet.getRelative(BlockFace.UP).getType();
        Material belowType = feet.getRelative(BlockFace.DOWN).getType();
        return belowType.isSolid() && !feetType.isSolid() && !headType.isSolid();
    }
}
