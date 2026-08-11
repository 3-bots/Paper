package com.hardcorerevival.plugin.commands;

import com.hardcorerevival.plugin.HardcoreRevivalPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public final class HardcoreRevivalCommand implements CommandExecutor {

    private final HardcoreRevivalPlugin plugin;

    public HardcoreRevivalCommand(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("clearpending")) {
            return handleClearPending(sender, label, args);
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /" + label + " <reload|clearpending <player>>");
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

    private boolean handleClearPending(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("hardcorerevival.clearpending")) {
            sender.sendMessage("You do not have permission to do that.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /" + label + " clearpending <player>");
            return true;
        }

        UUID targetId = plugin.getPendingRevivalRegistry().findByStoredName(args[1]);
        if (targetId == null) {
            sender.sendMessage("No one by that name is marked as awaiting revival.");
            return true;
        }

        String name = plugin.getPendingRevivalRegistry().nameOf(targetId);
        plugin.getPendingRevivalRegistry().removePending(targetId);
        sender.sendMessage(name + " is no longer marked as awaiting revival.");
        return true;
    }
}
