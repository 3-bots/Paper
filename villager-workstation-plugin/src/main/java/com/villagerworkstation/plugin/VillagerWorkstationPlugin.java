package com.villagerworkstation.plugin;

import com.villagerworkstation.plugin.commands.VillagerWorkstationCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class VillagerWorkstationPlugin extends JavaPlugin {

    private long checkIntervalTicks = 100L;
    private double maxDistance = 1.5;
    private boolean onlyDaytime = true;

    private BukkitTask task;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        var command = getCommand("villagerworkstation");
        if (command != null) {
            command.setExecutor(new VillagerWorkstationCommand(this));
        }

        startTask();
    }

    @Override
    public void onDisable() {
        stopTask();
    }

    public void loadSettings() {
        reloadConfig();
        var config = getConfig();

        checkIntervalTicks = Math.max(1L, config.getLong("settings.check-interval-ticks", 100));
        maxDistance = Math.max(0.1, config.getDouble("settings.max-distance", 1.5));
        onlyDaytime = config.getBoolean("settings.only-daytime", true);
    }

    public void restart() {
        stopTask();
        loadSettings();
        startTask();
    }

    private void startTask() {
        task = getServer().getScheduler().runTaskTimer(this, new WorkstationSnapTask(this), checkIntervalTicks, checkIntervalTicks);
    }

    private void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public boolean isOnlyDaytime() {
        return onlyDaytime;
    }
}
