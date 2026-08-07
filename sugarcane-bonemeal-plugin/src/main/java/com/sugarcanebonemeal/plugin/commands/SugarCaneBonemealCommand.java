package com.sugarcanebonemeal.plugin.commands;

import com.sugarcanebonemeal.plugin.SugarCaneBonemealPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class SugarCaneBonemealCommand implements CommandExecutor {

    private final SugarCaneBonemealPlugin plugin;

    public SugarCaneBonemealCommand(SugarCaneBonemealPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /" + label + " reload");
            return true;
        }

        if (!sender.hasPermission("sugarcanebonemeal.reload")) {
            sender.sendMessage("You do not have permission to do that.");
            return true;
        }

        plugin.loadSettings();
        sender.sendMessage("SugarCaneBonemeal configuration reloaded.");
        return true;
    }
}
