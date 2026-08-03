package com.simplehomes.plugin.commands;

import com.simplehomes.plugin.SimpleHomesPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class HomeCommand implements CommandExecutor {

    private final SimpleHomesPlugin plugin;

    public HomeCommand(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.message("player_only"));
            return true;
        }

        if (!player.hasPermission("simplehomes.home")) {
            player.sendMessage(plugin.message("no_permission"));
            return true;
        }

        boolean explicitName = args.length > 0;
        String name = explicitName ? args[0].toLowerCase() : SimpleHomesPlugin.DEFAULT_HOME_NAME;

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        Location target = homes.get(name);

        if (target == null) {
            if (!explicitName && homes.isEmpty()) {
                player.sendMessage(plugin.message("no_homes_set"));
            } else {
                player.sendMessage(plugin.message("home_not_found", explicitName ? args[0] : SimpleHomesPlugin.DEFAULT_HOME_NAME));
            }
            return true;
        }

        plugin.getTeleportManager().startTeleport(player, target);
        return true;
    }
}
