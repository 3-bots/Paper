package com.hardcorerevival.plugin.commands;

import com.hardcorerevival.plugin.HardcoreRevivalPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class HardcoreRevivalCommand implements CommandExecutor {

    private final HardcoreRevivalPlugin plugin;

    public HardcoreRevivalCommand(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /" + label + " reload");
            return true;
        }
        if (!sender.hasPermission("hardcorerevival.reload")) {
            sender.sendMessage("You do not have permission to do that.");
            return true;
        }
        plugin.restart();
        sender.sendMessage("HardcoreRevival configuration reloaded.");
        return true;
    }
}
