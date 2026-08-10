package com.simplehomes.plugin.commands;

import com.simplehomes.plugin.SimpleHomesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Handles both /tpa (come to me -> them) and /tpahere (come to me), distinguished by the "here" flag. */
public final class TpaCommand implements CommandExecutor {

    private final SimpleHomesPlugin plugin;
    private final boolean here;

    public TpaCommand(SimpleHomesPlugin plugin, boolean here) {
        this.plugin = plugin;
        this.here = here;
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

        if (args.length == 0) {
            player.sendMessage(plugin.message(here ? "usage_tpahere" : "usage_tpa"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.message("player_not_found", args[0]));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.message("tpa_self"));
            return true;
        }

        plugin.getTpaManager().request(player, target, here);

        player.sendMessage(plugin.message(here ? "tpahere_sent" : "tpa_sent", target.getName()));
        target.sendMessage(plugin.message(here ? "tpahere_received" : "tpa_received", player.getName()));
        return true;
    }
}
