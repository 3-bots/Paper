package com.hardcorerevival.plugin;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

/**
 * Fakes a "the sky is turning red" warning for one player without touching the
 * real world border: a personal WorldBorder override (Player#setWorldBorder) is
 * huge and centered on them, but its warning distance is set even bigger than
 * that - vanilla tints the whole screen red once a player is within the warning
 * distance of any edge, so with a warning distance larger than the border
 * itself, that's true everywhere inside it. Player#setWorldBorder(null) hands
 * the player back their real border (and the normal-colored screen) afterward.
 */
public final class RedSkyEffect {

    private static final double BORDER_SIZE = 2_000_000.0;
    private static final int WARNING_DISTANCE = 2_000_000;

    private RedSkyEffect() {
    }

    public static void apply(Player player) {
        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(player.getLocation());
        border.setSize(BORDER_SIZE);
        border.setWarningDistance(WARNING_DISTANCE);
        player.setWorldBorder(border);
    }

    public static void clear(Player player) {
        player.setWorldBorder(null);
    }
}
