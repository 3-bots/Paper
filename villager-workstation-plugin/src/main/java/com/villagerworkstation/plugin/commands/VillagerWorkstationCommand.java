package com.villagerworkstation.plugin.commands;

import com.villagerworkstation.plugin.VillagerWorkstationPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class VillagerWorkstationCommand implements CommandExecutor {

    private final VillagerWorkstationPlugin plugin;

    public VillagerWorkstationCommand(VillagerWorkstationPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /" + label + " reload");
            return true;
        }

        if (!sender.hasPermission("villagerworkstation.reload")) {
            sender.sendMessage("You do not have permission to do that.");
            return true;
        }

        plugin.restart();
        sender.sendMessage("VillagerWorkstation configuration reloaded.");
        return true;
    }
}
