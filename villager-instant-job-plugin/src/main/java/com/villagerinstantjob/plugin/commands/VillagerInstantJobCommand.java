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
        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " <reload|scan>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("villagerinstantjob.reload")) {
                    sender.sendMessage("You do not have permission to do that.");
                    return true;
                }
                plugin.restart();
                sender.sendMessage("VillagerInstantJob configuration reloaded.");
            }
            case "scan" -> {
                if (!sender.hasPermission("villagerinstantjob.reload")) {
                    sender.sendMessage("You do not have permission to do that.");
                    return true;
                }
                plugin.runScanNow();
                sender.sendMessage("Ran a manual sweep for jobless villagers - check console for results.");
            }
            default -> sender.sendMessage("Usage: /" + label + " <reload|scan>");
        }
        return true;
    }
}
