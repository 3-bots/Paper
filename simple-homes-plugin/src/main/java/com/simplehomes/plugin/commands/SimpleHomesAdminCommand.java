package com.simplehomes.plugin.commands;

import com.simplehomes.plugin.SimpleHomesPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class SimpleHomesAdminCommand implements CommandExecutor {

    private final SimpleHomesPlugin plugin;

    public SimpleHomesAdminCommand(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /" + label + " reload");
            return true;
        }

        if (!sender.hasPermission("simplehomes.reload")) {
            sender.sendMessage(plugin.message("no_permission"));
            return true;
        }

        plugin.loadSettings();
        sender.sendMessage(plugin.message("reload_success"));
        return true;
    }
}
