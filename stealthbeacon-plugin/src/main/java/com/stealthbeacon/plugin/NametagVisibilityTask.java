package com.stealthbeacon.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;

/**
 * Keeps a scoreboard team with NAME_TAG_VISIBILITY=NEVER in sync with which
 * online players are currently standing inside any active beacon's zone.
 * Note: since Bukkit only allows a player on one team at a time, this will
 * remove a player from any other team (e.g. a rank-color team) while their
 * nametag is hidden.
 */
public final class NametagVisibilityTask implements Runnable {

    private static final String TEAM_NAME = "stealthbeacon_hidden";

    private final StealthBeaconPlugin plugin;
    private final Set<String> currentlyHidden = new HashSet<>();

    public NametagVisibilityTask(StealthBeaconPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Scoreboard scoreboard = mainScoreboard();
        if (scoreboard == null) {
            return;
        }
        Team team = getOrCreateTeam(scoreboard);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            boolean inZone = isInAnyBeaconZone(player.getLocation());
            boolean currentlyOnTeam = currentlyHidden.contains(player.getName());

            if (inZone && !currentlyOnTeam) {
                team.addEntry(player.getName());
                currentlyHidden.add(player.getName());
            } else if (!inZone && currentlyOnTeam) {
                team.removeEntry(player.getName());
                currentlyHidden.remove(player.getName());
            }
        }
    }

    /** Restores everyone's nametag on disable/reload, since the plugin is what's hiding them. */
    public void clearAll() {
        Scoreboard scoreboard = mainScoreboard();
        if (scoreboard != null) {
            Team team = scoreboard.getTeam(TEAM_NAME);
            if (team != null) {
                for (String entry : Set.copyOf(team.getEntries())) {
                    team.removeEntry(entry);
                }
            }
        }
        currentlyHidden.clear();
    }

    private Scoreboard mainScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        return manager != null ? manager.getMainScoreboard() : null;
    }

    private Team getOrCreateTeam(Scoreboard scoreboard) {
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.registerNewTeam(TEAM_NAME);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        return team;
    }

    private boolean isInAnyBeaconZone(Location playerLocation) {
        for (BeaconRegistry.Beacon beacon : plugin.getBeaconRegistry().getActiveBeacons()) {
            Location center = beacon.location();
            if (center.getWorld() == null || !center.getWorld().equals(playerLocation.getWorld())) {
                continue;
            }

            double horizontalRadius = plugin.getHorizontalRadius(beacon.tier());
            double verticalRadius = plugin.getVerticalRadius(beacon.tier());

            double dy = playerLocation.getY() - center.getY();
            if (dy < -verticalRadius || dy > verticalRadius) {
                continue;
            }

            double dx = playerLocation.getX() - center.getX();
            double dz = playerLocation.getZ() - center.getZ();
            if (dx * dx + dz * dz <= horizontalRadius * horizontalRadius) {
                return true;
            }
        }
        return false;
    }
}
