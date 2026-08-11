package com.hardcorerevival.plugin;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

/**
 * If a player marked as awaiting revival ends up out of spectator mode by any
 * means other than the ritual completing - an op's /gamemode command, another
 * plugin, anything - this clears their pending status instead of leaving a
 * stale entry behind. Without this, an already-alive player stays forever
 * "awaiting revival" as far as the registry is concerned: books with their
 * name keep matching, the sky keeps turning red for them near an altar, and
 * the sign-book chat feedback keeps treating them as still dead.
 */
public final class PendingCleanupListener implements Listener {

    private final HardcoreRevivalPlugin plugin;

    public PendingCleanupListener(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Player player = event.getPlayer();
        if (plugin.getPendingRevivalRegistry().isPending(player.getUniqueId())) {
            plugin.getPendingRevivalRegistry().removePending(player.getUniqueId());
        }
    }
}
