package com.leashteleport.plugin;

import com.leashteleport.plugin.commands.LeashTeleportCommand;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class LeashTeleportPlugin extends JavaPlugin {

    private double searchRadius = 15.0;
    private double spreadRadius = 2.0;
    private boolean notify = true;
    private final Map<String, String> messages = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        getServer().getPluginManager().registerEvents(new PlayerTeleportListener(this), this);

        var command = getCommand("leashteleport");
        if (command != null) {
            command.setExecutor(new LeashTeleportCommand(this));
        }
    }

    public void loadSettings() {
        reloadConfig();
        var config = getConfig();

        searchRadius = config.getDouble("settings.search-radius", 15.0);
        spreadRadius = config.getDouble("settings.spread-radius", 2.0);
        notify = config.getBoolean("settings.notify", true);

        messages.clear();
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, messagesSection.getString(key, ""));
            }
        }
    }

    public String message(String key, Object... args) {
        String raw = messages.getOrDefault(key, key);
        String formatted = args.length > 0 ? String.format(raw, args) : raw;
        String prefix = messages.getOrDefault("prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + " " + formatted);
    }

    public double getSearchRadius() {
        return searchRadius;
    }

    public double getSpreadRadius() {
        return spreadRadius;
    }

    public boolean isNotifyEnabled() {
        return notify;
    }
}
