package com.itemdurabilitydisplay.plugin.commands;

import com.itemdurabilitydisplay.plugin.ItemDurabilityDisplayPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class ItemDurabilityDisplayCommand implements CommandExecutor {

    private final ItemDurabilityDisplayPlugin plugin;

    public ItemDurabilityDisplayCommand(ItemDurabilityDisplayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /" + label + " reload");
            return true;
        }
        if (!sender.hasPermission("itemdurabilitydisplay.reload")) {
            sender.sendMessage("You do not have permission to do that.");
            return true;
        }
        plugin.restart();
        sender.sendMessage("ItemDurabilityDisplay configuration reloaded.");
        return true;
    }
}
