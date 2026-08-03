package com.simplehomes.plugin.commands;

import com.simplehomes.plugin.SimpleHomesPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class HomesCommand implements CommandExecutor {

    private final SimpleHomesPlugin plugin;

    public HomesCommand(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.message("player_only"));
            return true;
        }

        if (!player.hasPermission("simplehomes.homes")) {
            player.sendMessage(plugin.message("no_permission"));
            return true;
        }

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        if (homes.isEmpty()) {
            player.sendMessage(plugin.message("no_homes_set"));
            return true;
        }

        player.sendMessage(plugin.message("homes_list_title"));
        if (homes.containsKey(SimpleHomesPlugin.DEFAULT_HOME_NAME)) {
            player.sendMessage(plugin.message("default_home"));
        }

        boolean hasCustom = homes.keySet().stream().anyMatch(n -> !n.equals(SimpleHomesPlugin.DEFAULT_HOME_NAME));
        if (hasCustom) {
            player.sendMessage(plugin.message("custom_homes"));
            for (String name : homes.keySet()) {
                if (!name.equals(SimpleHomesPlugin.DEFAULT_HOME_NAME)) {
                    player.sendMessage(" - " + name);
                }
            }
        }
        return true;
    }
}
