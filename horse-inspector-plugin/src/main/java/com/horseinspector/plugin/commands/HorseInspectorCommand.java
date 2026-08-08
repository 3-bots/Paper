package com.horseinspector.plugin.commands;

import com.horseinspector.plugin.HorseInspectorPlugin;
import com.horseinspector.plugin.InspectorTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class HorseInspectorCommand implements CommandExecutor {

    private final HorseInspectorPlugin plugin;
    private final InspectorTool tool;

    public HorseInspectorCommand(HorseInspectorPlugin plugin, InspectorTool tool) {
        this.plugin = plugin;
        this.tool = tool;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " <give|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can be given the tool.");
                    return true;
                }
                if (!sender.hasPermission("horseinspector.give")) {
                    sender.sendMessage("You do not have permission to do that.");
                    return true;
                }
                player.getInventory().addItem(tool.createItem());
                player.sendMessage(plugin.prefixed("&fHere's your Horse Inspector."));
            }
            case "reload" -> {
                if (!sender.hasPermission("horseinspector.reload")) {
                    sender.sendMessage("You do not have permission to do that.");
                    return true;
                }
                plugin.loadSettings();
                sender.sendMessage(plugin.prefixed("&aConfiguration reloaded."));
            }
            default -> sender.sendMessage("Usage: /" + label + " <give|reload>");
        }
        return true;
    }
}
