package com.itemdurabilitydisplay.plugin;

import com.itemdurabilitydisplay.plugin.commands.ItemDurabilityDisplayCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class ItemDurabilityDisplayPlugin extends JavaPlugin {

    private long updateIntervalTicks = 10L;

    private BukkitTask updateTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        var command = getCommand("itemdurabilitydisplay");
        if (command != null) {
            command.setExecutor(new ItemDurabilityDisplayCommand(this));
        }

        startUpdateTask();
    }

    @Override
    public void onDisable() {
        stopUpdateTask();
    }

    public void loadSettings() {
        reloadConfig();
        updateIntervalTicks = Math.max(1L, getConfig().getLong("settings.update-interval-ticks", 10));
    }

    public void restart() {
        stopUpdateTask();
        loadSettings();
        startUpdateTask();
    }

    private void startUpdateTask() {
        updateTask = getServer().getScheduler().runTaskTimer(this, new DurabilityDisplayTask(this),
                updateIntervalTicks, updateIntervalTicks);
    }

    private void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
}
