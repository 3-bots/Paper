package com.horseinspector.plugin;

import com.horseinspector.plugin.commands.HorseInspectorCommand;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HorseInspectorPlugin extends JavaPlugin {

    private InspectorTool tool;
    private final Map<UUID, Selection> selections = new HashMap<>();

    private boolean recipeEnabled = true;
    private long selectionTimeoutMillis = 60_000L;
    private double varianceFraction = 0.15;

    private double healthMin = 15.0;
    private double healthMax = 30.0;
    private double speedMin = 0.1125;
    private double speedMax = 0.3375;
    private double jumpMin = 0.4;
    private double jumpMax = 1.0;

    private String prefix = "";

    public record Selection(UUID horseId, long expiresAt) {
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        tool = new InspectorTool(this);
        if (recipeEnabled) {
            tool.registerRecipe(this);
        }

        getServer().getPluginManager().registerEvents(new HorseInteractListener(this, tool), this);

        var command = getCommand("horseinspector");
        if (command != null) {
            command.setExecutor(new HorseInspectorCommand(this, tool));
        }
    }

    public void loadSettings() {
        reloadConfig();
        var config = getConfig();

        recipeEnabled = config.getBoolean("settings.recipe-enabled", true);
        selectionTimeoutMillis = config.getLong("settings.selection-timeout-seconds", 60) * 1000L;
        varianceFraction = config.getDouble("settings.variance-fraction", 0.15);

        healthMin = config.getDouble("ranges.health.min", 15.0);
        healthMax = config.getDouble("ranges.health.max", 30.0);
        speedMin = config.getDouble("ranges.speed.min", 0.1125);
        speedMax = config.getDouble("ranges.speed.max", 0.3375);
        jumpMin = config.getDouble("ranges.jump.min", 0.4);
        jumpMax = config.getDouble("ranges.jump.max", 1.0);

        prefix = ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix", ""));
    }

    public void setSelection(UUID player, UUID horseId) {
        selections.put(player, new Selection(horseId, System.currentTimeMillis() + selectionTimeoutMillis));
    }

    public Selection getSelection(UUID player) {
        Selection selection = selections.get(player);
        if (selection == null) {
            return null;
        }
        if (System.currentTimeMillis() > selection.expiresAt()) {
            selections.remove(player);
            return null;
        }
        return selection;
    }

    public void clearSelection(UUID player) {
        selections.remove(player);
    }

    public String prefixed(String message) {
        return prefix + " " + ChatColor.translateAlternateColorCodes('&', message);
    }

    public double getVarianceFraction() {
        return varianceFraction;
    }

    public double getHealthMin() {
        return healthMin;
    }

    public double getHealthMax() {
        return healthMax;
    }

    public double getSpeedMin() {
        return speedMin;
    }

    public double getSpeedMax() {
        return speedMax;
    }

    public double getJumpMin() {
        return jumpMin;
    }

    public double getJumpMax() {
        return jumpMax;
    }
}
