package com.leashteleport.plugin.commands;

import com.leashteleport.plugin.LeashTeleportPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class LeashTeleportCommand implements CommandExecutor {

    private final LeashTeleportPlugin plugin;

    public LeashTeleportCommand(LeashTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /" + label + " reload");
            return true;
        }

        if (!sender.hasPermission("leashteleport.reload")) {
            sender.sendMessage(plugin.message("no-permission"));
            return true;
        }

        plugin.loadSettings();
        sender.sendMessage(plugin.message("reload-success"));
        return true;
    }
}
