package com.simplehomes.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

/** Blocks vanilla's spectator-menu teleport (clicking a player in the tab list to jump to them). */
public final class SpectatorTeleportListener implements Listener {

    private final SimpleHomesPlugin plugin;

    public SpectatorTeleportListener(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        player.sendMessage(plugin.message("spectate_menu_blocked"));
    }
}
