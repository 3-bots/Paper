package com.simplehomes.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerMoveListener implements Listener {

    private final TeleportManager teleportManager;
    private final TpaManager tpaManager;

    public PlayerMoveListener(TeleportManager teleportManager, TpaManager tpaManager) {
        this.teleportManager = teleportManager;
        this.tpaManager = tpaManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!teleportManager.isPending(player.getUniqueId())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            teleportManager.cancelTeleport(player.getUniqueId(), true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        teleportManager.cancelTeleport(event.getPlayer().getUniqueId(), false);
        tpaManager.clearInvolving(event.getPlayer().getUniqueId());
    }
}
