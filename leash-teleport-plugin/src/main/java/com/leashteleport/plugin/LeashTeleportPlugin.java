package com.leashteleport.plugin;

import com.leashteleport.plugin.commands.LeashTeleportCommand;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class LeashTeleportPlugin extends JavaPlugin {

    private double teleportDetectBlocks = 20.0;
    private int detectWindowSeconds = 5;
    private double spreadRadius = 2.0;
    private boolean notify = true;
    private final Map<String, String> messages = new HashMap<>();

    private PlayerPositionTracker tracker;
    private LeashRescueListener rescueListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        tracker = new PlayerPositionTracker(this);
        tracker.start();

        rescueListener = new LeashRescueListener(this, tracker);
        getServer().getPluginManager().registerEvents(rescueListener, this);
        rescueListener.start();

        getServer().getPluginManager().registerEvents(new VehicleTeleportListener(this), this);

        var command = getCommand("leashteleport");
        if (command != null) {
            command.setExecutor(new LeashTeleportCommand(this));
        }

        getLogger().info("LeashTeleport " + getDescription().getVersion() + " enabled - villager leashing, teleport rescue, and mount teleport active.");
    }

    @Override
    public void onDisable() {
        if (tracker != null) {
            tracker.stop();
        }
        if (rescueListener != null) {
            rescueListener.stop();
        }
    }

    public void loadSettings() {
        reloadConfig();
        var config = getConfig();

        teleportDetectBlocks = config.getDouble("settings.teleport-detect-blocks", 20.0);
        detectWindowSeconds = config.getInt("settings.detect-window-seconds", 5);
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

    public double getTeleportDetectBlocks() {
        return teleportDetectBlocks;
    }

    public int getDetectWindowSeconds() {
        return detectWindowSeconds;
    }

    public double getSpreadRadius() {
        return spreadRadius;
    }

    public boolean isNotifyEnabled() {
        return notify;
    }
}
