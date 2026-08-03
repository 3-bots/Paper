package com.simplehomes.plugin.commands;

import com.simplehomes.plugin.SimpleHomesPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class DelHomeCommand implements CommandExecutor {

    private final SimpleHomesPlugin plugin;

    public DelHomeCommand(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.message("player_only"));
            return true;
        }

        if (!player.hasPermission("simplehomes.delhome")) {
            player.sendMessage(plugin.message("no_permission"));
            return true;
        }

        boolean explicitName = args.length > 0;
        String name = explicitName ? args[0].toLowerCase() : SimpleHomesPlugin.DEFAULT_HOME_NAME;

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());

        if (!homes.containsKey(name)) {
            player.sendMessage(plugin.message("home_not_found", explicitName ? args[0] : SimpleHomesPlugin.DEFAULT_HOME_NAME));
            return true;
        }

        homes.remove(name);
        plugin.getHomeManager().saveHomes(player.getUniqueId());

        if (name.equals(SimpleHomesPlugin.DEFAULT_HOME_NAME)) {
            player.sendMessage(plugin.message("default_home_deleted"));
        } else {
            player.sendMessage(plugin.message("custom_home_deleted", args[0]));
        }
        return true;
    }
}
