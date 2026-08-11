package com.simplehomes.plugin.commands;

import com.simplehomes.plugin.SimpleHomesPlugin;
import com.simplehomes.plugin.TpaManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TpAcceptCommand implements CommandExecutor {

    private final SimpleHomesPlugin plugin;

    public TpAcceptCommand(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.message("player_only"));
            return true;
        }

        if (!player.hasPermission("simplehomes.tpa")) {
            player.sendMessage(plugin.message("no_permission"));
            return true;
        }

        TpaManager.PendingRequest pending = plugin.getTpaManager().getPending(player.getUniqueId());
        if (pending == null) {
            player.sendMessage(plugin.message("no_pending_tpa"));
            return true;
        }

        Player requester = Bukkit.getPlayer(pending.requesterId());
        plugin.getTpaManager().clear(player.getUniqueId());

        if (requester == null || !requester.isOnline()) {
            player.sendMessage(plugin.message("tpa_requester_offline"));
            return true;
        }

        Player moving = pending.here() ? player : requester;
        Player staying = pending.here() ? requester : player;

        if (moving.getGameMode() == GameMode.SPECTATOR || staying.getGameMode() == GameMode.SPECTATOR) {
            moving.sendMessage(plugin.message("tpa_spectator_cancelled"));
            staying.sendMessage(plugin.message("tpa_spectator_cancelled"));
            return true;
        }

        moving.sendMessage(plugin.message("tpa_accepted_moving", staying.getName()));
        staying.sendMessage(plugin.message("tpa_accepted_waiting", moving.getName()));

        plugin.getTeleportManager().startTeleport(moving, staying.getLocation());
        return true;
    }
}
