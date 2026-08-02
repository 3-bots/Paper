package com.daysdisplay.plugin;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DaysDisplayPlugin extends JavaPlugin {

    private static final String OBJECTIVE_NAME = "daysdisplay";

    public enum DisplayMode { ACTIONBAR, BOSSBAR, SCOREBOARD }

    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();
    private final Map<UUID, Scoreboard> activeScoreboards = new HashMap<>();
    private final Set<UUID> disabledPlayers = new HashSet<>();

    private DisplayMode displayMode = DisplayMode.ACTIONBAR;
    private long updateIntervalTicks = 20L;
    private String format = "&eDay &f%day%";
    private long dayOffset = 1L;
    private BossBar.Color bossBarColor = BossBar.Color.YELLOW;
    private String scoreboardTitle = "&6&lServer Info";

    private BukkitTask updateTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        getServer().getPluginManager().registerEvents(new DaysDisplayListener(this), this);

        var command = getCommand("daysdisplay");
        if (command != null) {
            DaysDisplayCommand executor = new DaysDisplayCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        for (Player player : getServer().getOnlinePlayers()) {
            initializePlayer(player);
        }

        startUpdateTask();
    }

    @Override
    public void onDisable() {
        stopUpdateTask();
        for (Player player : getServer().getOnlinePlayers()) {
            clearPlayer(player);
        }
    }

    public void reload() {
        stopUpdateTask();
        for (Player player : getServer().getOnlinePlayers()) {
            clearPlayer(player);
        }
        loadSettings();
        for (Player player : getServer().getOnlinePlayers()) {
            initializePlayer(player);
        }
        startUpdateTask();
    }

    private void loadSettings() {
        reloadConfig();
        var config = getConfig();

        try {
            displayMode = DisplayMode.valueOf(config.getString("display-mode", "ACTIONBAR").toUpperCase());
        } catch (IllegalArgumentException ex) {
            getLogger().warning("Invalid display-mode in config.yml, defaulting to ACTIONBAR");
            displayMode = DisplayMode.ACTIONBAR;
        }

        updateIntervalTicks = Math.max(1L, config.getLong("update-interval-ticks", 20L));
        format = config.getString("format", "&eDay &f%day%");
        dayOffset = config.getLong("day-offset", 1L);
        scoreboardTitle = config.getString("scoreboard-title", "&6&lServer Info");

        try {
            bossBarColor = BossBar.Color.valueOf(config.getString("boss-bar-color", "YELLOW").toUpperCase());
        } catch (IllegalArgumentException ex) {
            getLogger().warning("Invalid boss-bar-color in config.yml, defaulting to YELLOW");
            bossBarColor = BossBar.Color.YELLOW;
        }
    }

    private void startUpdateTask() {
        updateTask = getServer().getScheduler().runTaskTimer(this, this::updateAllPlayers, 0L, updateIntervalTicks);
    }

    private void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    private void updateAllPlayers() {
        for (Player player : getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    public void initializePlayer(Player player) {
        if (disabledPlayers.contains(player.getUniqueId())) {
            return;
        }

        if (displayMode == DisplayMode.BOSSBAR) {
            BossBar bar = BossBar.bossBar(Component.empty(), 1.0f, bossBarColor, BossBar.Overlay.PROGRESS);
            activeBossBars.put(player.getUniqueId(), bar);
            player.showBossBar(bar);
        } else if (displayMode == DisplayMode.SCOREBOARD) {
            ScoreboardManager manager = getServer().getScoreboardManager();
            if (manager != null) {
                Scoreboard scoreboard = manager.getNewScoreboard();
                Objective objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, color(scoreboardTitle));
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                activeScoreboards.put(player.getUniqueId(), scoreboard);
                player.setScoreboard(scoreboard);
            }
        }

        updatePlayer(player);
    }

    public void clearPlayer(Player player) {
        UUID id = player.getUniqueId();

        BossBar bar = activeBossBars.remove(id);
        if (bar != null) {
            player.hideBossBar(bar);
        }

        if (activeScoreboards.remove(id) != null) {
            ScoreboardManager manager = getServer().getScoreboardManager();
            if (manager != null) {
                player.setScoreboard(manager.getMainScoreboard());
            }
        }
    }

    private void updatePlayer(Player player) {
        if (disabledPlayers.contains(player.getUniqueId())) {
            return;
        }

        long day = computeDay(player.getWorld());
        String text = format.replace("%day%", String.valueOf(day));
        Component component = color(text);

        switch (displayMode) {
            case ACTIONBAR -> player.sendActionBar(component);
            case BOSSBAR -> {
                BossBar bar = activeBossBars.get(player.getUniqueId());
                if (bar != null) {
                    bar.name(component);
                }
            }
            case SCOREBOARD -> {
                Scoreboard scoreboard = activeScoreboards.get(player.getUniqueId());
                if (scoreboard != null) {
                    Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
                    if (objective != null) {
                        for (String entry : scoreboard.getEntries()) {
                            scoreboard.resetScores(entry);
                        }
                        objective.getScore(text).setScore(0);
                    }
                }
            }
        }
    }

    public long computeDay(World world) {
        return (world.getFullTime() / 24000L) + dayOffset;
    }

    public boolean toggle(Player player) {
        UUID id = player.getUniqueId();
        if (disabledPlayers.contains(id)) {
            disabledPlayers.remove(id);
            initializePlayer(player);
            return true;
        } else {
            disabledPlayers.add(id);
            clearPlayer(player);
            return false;
        }
    }

    public static Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
