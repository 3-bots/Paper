package com.spectatorpossession.plugin;

import com.spectatorpossession.plugin.commands.SpectatorPossessionCommand;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class SpectatorPossessionPlugin extends JavaPlugin {

    private boolean explosionBreaksBlocks = false;
    private final Map<String, String> messages = new HashMap<>();

    private PossessionManager possessionManager;
    private AbilityRegistry abilityRegistry;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        possessionManager = new PossessionManager(this);
        abilityRegistry = new AbilityRegistry(this);

        getServer().getPluginManager().registerEvents(new PossessListener(this), this);

        var command = getCommand("possess");
        if (command != null) {
            command.setExecutor(new SpectatorPossessionCommand(this));
        }
    }

    public void loadSettings() {
        reloadConfig();
        var config = getConfig();

        explosionBreaksBlocks = config.getBoolean("settings.creeper-explosion-breaks-blocks", false);

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

    public boolean isExplosionBreaksBlocks() {
        return explosionBreaksBlocks;
    }

    public PossessionManager getPossessionManager() {
        return possessionManager;
    }

    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }
}
