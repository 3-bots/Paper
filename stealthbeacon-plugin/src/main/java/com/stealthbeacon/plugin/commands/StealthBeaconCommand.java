package com.stealthbeacon.plugin.commands;

import com.stealthbeacon.plugin.StealthBeaconPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class StealthBeaconCommand implements CommandExecutor {

    private final StealthBeaconPlugin plugin;

    public StealthBeaconCommand(StealthBeaconPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /" + label + " reload");
            return true;
        }
        if (!sender.hasPermission("stealthbeacon.reload")) {
            sender.sendMessage("You do not have permission to do that.");
            return true;
        }
        plugin.restart();
        sender.sendMessage("StealthBeacon configuration reloaded.");
        return true;
    }
}
