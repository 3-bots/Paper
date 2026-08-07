package com.sugarcanebonemeal.plugin;

import com.sugarcanebonemeal.plugin.commands.SugarCaneBonemealCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class SugarCaneBonemealPlugin extends JavaPlugin {

    private int maxHeight = 3;
    private boolean playEffects = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        getServer().getPluginManager().registerEvents(new DispenserBonemealListener(this), this);

        var command = getCommand("sugarcanebonemeal");
        if (command != null) {
            command.setExecutor(new SugarCaneBonemealCommand(this));
        }
    }

    public void loadSettings() {
        reloadConfig();
        var config = getConfig();

        maxHeight = Math.max(1, config.getInt("settings.max-height", 3));
        playEffects = config.getBoolean("settings.play-effects", true);
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public boolean isPlayEffectsEnabled() {
        return playEffects;
    }
}
