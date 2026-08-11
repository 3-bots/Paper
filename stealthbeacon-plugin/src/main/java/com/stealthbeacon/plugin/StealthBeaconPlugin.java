package com.stealthbeacon.plugin;

import com.stealthbeacon.plugin.commands.StealthBeaconCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StealthBeaconPlugin extends JavaPlugin {

    private static final double[] DEFAULT_HORIZONTAL_RADII = {10, 20, 35, 50};
    private static final double[] DEFAULT_VERTICAL_RADII = {10, 15, 20, 25};

    private Material markerMaterial = Material.SEA_LANTERN;
    private long updateIntervalTicks = 20L;
    private final double[] horizontalRadii = new double[BeaconRegistry.MAX_TIER];
    private final double[] verticalRadii = new double[BeaconRegistry.MAX_TIER];
    private final Map<String, String> messages = new HashMap<>();

    private BeaconRegistry beaconRegistry;
    private NametagVisibilityTask visibilityTask;
    private BukkitTask scheduledTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        beaconRegistry = new BeaconRegistry(this);
        beaconRegistry.revalidate();

        getServer().getPluginManager().registerEvents(new BeaconBlockListener(this), this);

        var command = getCommand("stealthbeacon");
        if (command != null) {
            command.setExecutor(new StealthBeaconCommand(this));
        }

        visibilityTask = new NametagVisibilityTask(this);
        startTask();
    }

    @Override
    public void onDisable() {
        stopTask();
        if (visibilityTask != null) {
            visibilityTask.clearAll();
        }
    }

    public void loadSettings() {
        reloadConfig();
        var config = getConfig();

        Material material = Material.matchMaterial(config.getString("settings.marker-material", "SEA_LANTERN"));
        markerMaterial = material != null ? material : Material.SEA_LANTERN;

        updateIntervalTicks = Math.max(1L, config.getLong("settings.update-interval-ticks", 20));

        List<Double> horizontal = config.getDoubleList("settings.tier-horizontal-radius");
        List<Double> vertical = config.getDoubleList("settings.tier-vertical-radius");
        for (int i = 0; i < BeaconRegistry.MAX_TIER; i++) {
            horizontalRadii[i] = i < horizontal.size() ? horizontal.get(i) : DEFAULT_HORIZONTAL_RADII[i];
            verticalRadii[i] = i < vertical.size() ? vertical.get(i) : DEFAULT_VERTICAL_RADII[i];
        }

        messages.clear();
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, messagesSection.getString(key, ""));
            }
        }
    }

    public void restart() {
        stopTask();
        loadSettings();
        if (beaconRegistry != null) {
            beaconRegistry.revalidate();
        }
        startTask();
    }

    private void startTask() {
        scheduledTask = getServer().getScheduler().runTaskTimer(this, () -> {
            beaconRegistry.revalidate();
            visibilityTask.run();
        }, updateIntervalTicks, updateIntervalTicks);
    }

    private void stopTask() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
    }

    public String message(String key, Object... args) {
        String raw = messages.getOrDefault(key, key);
        String formatted = args.length > 0 ? String.format(raw, args) : raw;
        String prefix = messages.getOrDefault("prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + " " + formatted);
    }

    public Material getMarkerMaterial() {
        return markerMaterial;
    }

    public double getHorizontalRadius(int tier) {
        return horizontalRadii[clampTierIndex(tier)];
    }

    public double getVerticalRadius(int tier) {
        return verticalRadii[clampTierIndex(tier)];
    }

    private int clampTierIndex(int tier) {
        return Math.max(1, Math.min(tier, BeaconRegistry.MAX_TIER)) - 1;
    }

    public BeaconRegistry getBeaconRegistry() {
        return beaconRegistry;
    }
}
