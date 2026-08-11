package com.hardcorerevival.plugin;

import com.hardcorerevival.plugin.commands.HardcoreRevivalCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public final class HardcoreRevivalPlugin extends JavaPlugin {

    private Material fireMaterial = Material.FIRE;
    private int altarRadius = 3;
    private int requiredIronBlocks = 4;
    private int requiredDiamondBlocks = 4;
    private int sacrificeWindowSeconds = 60;
    private double ritualBlockLossChance = 0.3;
    private final Map<String, String> messages = new HashMap<>();

    private PendingRevivalRegistry pendingRevivalRegistry;
    private AltarValidator altarValidator;
    private AltarFinder altarFinder;
    private RitualState ritualState;
    private EssenceEffect essenceEffect;
    private AltarConsumption altarConsumption;
    private BukkitTask glowTask;
    private BookGlowTask bookGlowTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        pendingRevivalRegistry = new PendingRevivalRegistry(this);
        altarValidator = new AltarValidator(this);
        altarFinder = new AltarFinder(this, altarValidator);
        ritualState = new RitualState();
        essenceEffect = new EssenceEffect(this);
        altarConsumption = new AltarConsumption(this);

        getServer().getPluginManager().registerEvents(new HardcoreDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new VillagerSacrificeListener(this), this);
        getServer().getPluginManager().registerEvents(new RitualFireListener(this), this);
        getServer().getPluginManager().registerEvents(new RitualSignListener(this), this);
        getServer().getPluginManager().registerEvents(new PendingCleanupListener(this), this);

        var command = getCommand("hardcorerevival");
        if (command != null) {
            command.setExecutor(new HardcoreRevivalCommand(this));
        }

        startGlowTask();
    }

    @Override
    public void onDisable() {
        stopGlowTask();
    }

    public void loadSettings() {
        reloadConfig();
        var config = getConfig();

        Material material = Material.matchMaterial(config.getString("settings.fire-material", "FIRE"));
        fireMaterial = material != null ? material : Material.FIRE;

        altarRadius = Math.max(1, config.getInt("settings.altar-radius", 3));
        requiredIronBlocks = Math.max(1, config.getInt("settings.iron-blocks-required", 4));
        requiredDiamondBlocks = Math.max(1, config.getInt("settings.diamond-blocks-required", 4));
        sacrificeWindowSeconds = Math.max(1, config.getInt("settings.sacrifice-window-seconds", 60));
        ritualBlockLossChance = Math.min(1.0, Math.max(0.0, config.getDouble("settings.ritual-block-loss-chance", 0.3)));

        messages.clear();
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, messagesSection.getString(key, ""));
            }
        }
    }

    public void restart() {
        stopGlowTask();
        loadSettings();
        startGlowTask();
    }

    private void startGlowTask() {
        bookGlowTask = new BookGlowTask(this);
        glowTask = getServer().getScheduler().runTaskTimer(this, bookGlowTask, 10L, 10L);
    }

    private void stopGlowTask() {
        if (glowTask != null) {
            glowTask.cancel();
            glowTask = null;
        }
        if (bookGlowTask != null) {
            bookGlowTask.clearAllRedSky();
            bookGlowTask = null;
        }
    }

    public String message(String key, Object... args) {
        String raw = messages.getOrDefault(key, key);
        String formatted = args.length > 0 ? String.format(raw, args) : raw;
        String prefix = messages.getOrDefault("prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + " " + formatted);
    }

    public Material getFireMaterial() {
        return fireMaterial;
    }

    public int getAltarRadius() {
        return altarRadius;
    }

    public int getRequiredIronBlocks() {
        return requiredIronBlocks;
    }

    public int getRequiredDiamondBlocks() {
        return requiredDiamondBlocks;
    }

    public int getSacrificeWindowSeconds() {
        return sacrificeWindowSeconds;
    }

    public double getRitualBlockLossChance() {
        return ritualBlockLossChance;
    }

    public PendingRevivalRegistry getPendingRevivalRegistry() {
        return pendingRevivalRegistry;
    }

    public AltarValidator getAltarValidator() {
        return altarValidator;
    }

    public AltarFinder getAltarFinder() {
        return altarFinder;
    }

    public RitualState getRitualState() {
        return ritualState;
    }

    public EssenceEffect getEssenceEffect() {
        return essenceEffect;
    }

    public AltarConsumption getAltarConsumption() {
        return altarConsumption;
    }
}
