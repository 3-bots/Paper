package com.spectatorpossession.plugin.commands;

import com.spectatorpossession.plugin.SpectatorPossessionPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SpectatorPossessionCommand implements CommandExecutor {

    private final SpectatorPossessionPlugin plugin;

    public SpectatorPossessionCommand(SpectatorPossessionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("spectatorpossession.reload")) {
                sender.sendMessage("You do not have permission to do that.");
                return true;
            }
            plugin.loadSettings();
            sender.sendMessage("SpectatorPossession configuration reloaded.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("release")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!plugin.getPossessionManager().isPossessing(player)) {
                sender.sendMessage("You are not possessing anything.");
                return true;
            }
            plugin.getPossessionManager().stopPossessing(player);
            sender.sendMessage(plugin.message("possess_released"));
            return true;
        }

        sender.sendMessage("Usage: /" + label + " <release|reload>");
        return true;
    }
}
