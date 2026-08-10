package com.simplehomes.plugin;

import com.simplehomes.plugin.commands.DelHomeCommand;
import com.simplehomes.plugin.commands.HomeCommand;
import com.simplehomes.plugin.commands.HomesCommand;
import com.simplehomes.plugin.commands.SetHomeCommand;
import com.simplehomes.plugin.commands.SimpleHomesAdminCommand;
import com.simplehomes.plugin.commands.TpAcceptCommand;
import com.simplehomes.plugin.commands.TpDenyCommand;
import com.simplehomes.plugin.commands.TpaCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class SimpleHomesPlugin extends JavaPlugin {

    public static final String DEFAULT_HOME_NAME = "default";

    private HomeManager homeManager;
    private TeleportManager teleportManager;
    private TpaManager tpaManager;

    private int teleportDelaySeconds = 3;
    private int maxHomes = 4;
    private int freeHomes = 1;
    private int tpaExpirySeconds = 120;
    private final TreeMap<Integer, List<CostEntry>> costs = new TreeMap<>();
    private final Map<String, String> messages = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        homeManager = new HomeManager(this);
        teleportManager = new TeleportManager(this);
        tpaManager = new TpaManager(this);

        getServer().getPluginManager().registerEvents(new PlayerMoveListener(teleportManager, tpaManager), this);

        setExecutor("sethome", new SetHomeCommand(this));
        setExecutor("delhome", new DelHomeCommand(this));
        setExecutor("home", new HomeCommand(this));
        setExecutor("homes", new HomesCommand(this));
        setExecutor("simplehomes", new SimpleHomesAdminCommand(this));
        setExecutor("tpa", new TpaCommand(this, false));
        setExecutor("tpahere", new TpaCommand(this, true));
        setExecutor("tpaccept", new TpAcceptCommand(this));
        setExecutor("tpdeny", new TpDenyCommand(this));
    }

    @Override
    public void onDisable() {
        teleportManager.cancelAll();
        tpaManager.cancelAll();
        homeManager.saveAll();
    }

    private void setExecutor(String name, org.bukkit.command.CommandExecutor executor) {
        var command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        }
    }

    public void loadSettings() {
        reloadConfig();
        var config = getConfig();

        teleportDelaySeconds = config.getInt("settings.teleport_delay", 3);
        maxHomes = Math.max(1, config.getInt("settings.max_homes", 4));
        freeHomes = Math.max(0, config.getInt("settings.free_homes", 1));
        tpaExpirySeconds = Math.max(0, config.getInt("settings.tpa_expiry", 120));

        costs.clear();
        ConfigurationSection costsSection = config.getConfigurationSection("costs");
        if (costsSection != null) {
            for (String key : costsSection.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(key);
                } catch (NumberFormatException ex) {
                    getLogger().warning("Invalid home slot number in costs: " + key);
                    continue;
                }

                List<CostEntry> entries = new ArrayList<>();
                for (Map<?, ?> raw : costsSection.getMapList(key)) {
                    Object materialName = raw.get("material");
                    Object amountValue = raw.get("amount");
                    if (materialName == null) {
                        continue;
                    }
                    Material material = Material.matchMaterial(materialName.toString());
                    if (material == null) {
                        getLogger().warning("Unknown material in costs for slot " + slot + ": " + materialName);
                        continue;
                    }
                    int amount = amountValue instanceof Number number ? number.intValue() : 1;
                    entries.add(new CostEntry(material, Math.max(1, amount)));
                }
                costs.put(slot, entries);
            }
        }

        messages.clear();
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, messagesSection.getString(key, ""));
            }
        }
    }

    public List<CostEntry> getCostFor(int homeSlot) {
        if (homeSlot <= freeHomes) {
            return List.of();
        }
        Map.Entry<Integer, List<CostEntry>> entry = costs.floorEntry(homeSlot);
        return entry != null ? entry.getValue() : List.of();
    }

    public String message(String key, Object... args) {
        String raw = messages.getOrDefault(key, key);
        String formatted = args.length > 0 ? String.format(raw, args) : raw;
        String prefix = messages.getOrDefault("prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + " " + formatted);
    }

    public String describeCosts(List<CostEntry> costEntries) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < costEntries.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            CostEntry entry = costEntries.get(i);
            builder.append(entry.amount()).append("x ").append(formatMaterial(entry.material()));
        }
        return builder.toString();
    }

    private String formatMaterial(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public TpaManager getTpaManager() {
        return tpaManager;
    }

    public int getTeleportDelaySeconds() {
        return teleportDelaySeconds;
    }

    public int getMaxHomes() {
        return maxHomes;
    }

    public int getTpaExpirySeconds() {
        return tpaExpirySeconds;
    }
}
