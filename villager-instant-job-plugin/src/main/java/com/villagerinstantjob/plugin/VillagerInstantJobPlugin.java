package com.villagerinstantjob.plugin;

import com.villagerinstantjob.plugin.commands.VillagerInstantJobCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class VillagerInstantJobPlugin extends JavaPlugin {

    private static final long PERSISTENCE_INTERVAL_TICKS = 20L;

    private double searchRadius = 16.0;
    private int scanBlockRadius = 8;
    private long scanIntervalTicks = 100L;

    private BukkitTask scanTask;
    private BukkitTask persistenceTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        getServer().getPluginManager().registerEvents(new JobBlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new JobCareerChangeListener(), this);

        var command = getCommand("villagerinstantjob");
        if (command != null) {
            VillagerInstantJobCommand executor = new VillagerInstantJobCommand(this);
            command.setExecutor(executor);
        }

        startScanTask();
        startPersistenceTask();
    }

    @Override
    public void onDisable() {
        stopScanTask();
        stopPersistenceTask();
    }

    public void loadSettings() {
        reloadConfig();
        var config = getConfig();
        searchRadius = Math.max(1.0, config.getDouble("settings.search-radius", 16.0));
        scanBlockRadius = Math.max(1, config.getInt("settings.scan-block-radius", 8));
        scanIntervalTicks = Math.max(20L, config.getLong("settings.scan-interval-ticks", 100));
    }

    public void restart() {
        stopScanTask();
        loadSettings();
        startScanTask();
    }

    private void startScanTask() {
        scanTask = getServer().getScheduler().runTaskTimer(this, new JoblessVillagerScanTask(this), scanIntervalTicks, scanIntervalTicks);
    }

    private void stopScanTask() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
    }

    private void startPersistenceTask() {
        persistenceTask = getServer().getScheduler().runTaskTimer(this, new JobPersistenceTask(this),
                PERSISTENCE_INTERVAL_TICKS, PERSISTENCE_INTERVAL_TICKS);
    }

    private void stopPersistenceTask() {
        if (persistenceTask != null) {
            persistenceTask.cancel();
            persistenceTask = null;
        }
    }

    public void runScanNow() {
        new JoblessVillagerScanTask(this).run();
    }

    public double getSearchRadius() {
        return searchRadius;
    }

    public int getScanBlockRadius() {
        return scanBlockRadius;
    }
}
