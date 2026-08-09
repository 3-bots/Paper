package com.villagerinstantjob.plugin.commands;

import com.villagerinstantjob.plugin.VillagerInstantJobPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class VillagerInstantJobCommand implements CommandExecutor {

    private final VillagerInstantJobPlugin plugin;

    public VillagerInstantJobCommand(VillagerInstantJobPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /" + label + " reload");
            return true;
        }

        if (!sender.hasPermission("villagerinstantjob.reload")) {
            sender.sendMessage("You do not have permission to do that.");
            return true;
        }

        plugin.loadSettings();
        sender.sendMessage("VillagerInstantJob configuration reloaded.");
        return true;
    }
}
