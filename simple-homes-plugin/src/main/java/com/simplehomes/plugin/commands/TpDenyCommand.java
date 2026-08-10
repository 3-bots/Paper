package com.simplehomes.plugin.commands;

import com.simplehomes.plugin.SimpleHomesPlugin;
import com.simplehomes.plugin.TpaManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TpDenyCommand implements CommandExecutor {

    private final SimpleHomesPlugin plugin;

    public TpDenyCommand(SimpleHomesPlugin plugin) {
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

        plugin.getTpaManager().clear(player.getUniqueId());

        player.sendMessage(plugin.message("tpa_denied_target"));
        Player requester = Bukkit.getPlayer(pending.requesterId());
        if (requester != null) {
            requester.sendMessage(plugin.message("tpa_denied_requester", player.getName()));
        }
        return true;
    }
}
