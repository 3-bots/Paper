package com.simplehomes.plugin.commands;

import com.simplehomes.plugin.SimpleHomesPlugin;
import org.bukkit.ChatColor;
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
        Location defaultHome = homes.get(SimpleHomesPlugin.DEFAULT_HOME_NAME);
        if (defaultHome != null) {
            player.sendMessage(plugin.message("default_home") + " " + describeLocation(defaultHome));
        }

        boolean hasCustom = homes.keySet().stream().anyMatch(n -> !n.equals(SimpleHomesPlugin.DEFAULT_HOME_NAME));
        if (hasCustom) {
            player.sendMessage(plugin.message("custom_homes"));
            for (Map.Entry<String, Location> entry : homes.entrySet()) {
                if (!entry.getKey().equals(SimpleHomesPlugin.DEFAULT_HOME_NAME)) {
                    player.sendMessage(" - " + entry.getKey() + " " + describeLocation(entry.getValue()));
                }
            }
        }
        return true;
    }

    private static String describeLocation(Location location) {
        return ChatColor.GRAY + "(" + location.getWorld().getName() + ": "
                + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
    }
}
