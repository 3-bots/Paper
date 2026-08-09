package com.villagerinstantjob.plugin;

import com.villagerinstantjob.plugin.commands.VillagerInstantJobCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class VillagerInstantJobPlugin extends JavaPlugin {

    private double searchRadius = 16.0;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        getServer().getPluginManager().registerEvents(new JobBlockPlaceListener(this), this);

        var command = getCommand("villagerinstantjob");
        if (command != null) {
            command.setExecutor(new VillagerInstantJobCommand(this));
        }
    }

    public void loadSettings() {
        reloadConfig();
        searchRadius = Math.max(1.0, getConfig().getDouble("settings.search-radius", 16.0));
    }

    public double getSearchRadius() {
        return searchRadius;
    }
}
