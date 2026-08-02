package com.daysdisplay.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class DaysDisplayCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "toggle");

    private final DaysDisplayPlugin plugin;

    public DaysDisplayCommand(DaysDisplayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + label + " <reload|toggle>", NamedTextColor.YELLOW));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("daysdisplay.reload")) {
                    sender.sendMessage(Component.text("You do not have permission to do that.", NamedTextColor.RED));
                    return true;
                }
                plugin.reload();
                sender.sendMessage(Component.text("DaysDisplay configuration reloaded.", NamedTextColor.GREEN));
            }
            case "toggle" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Only players can toggle the days display.", NamedTextColor.RED));
                    return true;
                }
                if (!sender.hasPermission("daysdisplay.toggle")) {
                    sender.sendMessage(Component.text("You do not have permission to do that.", NamedTextColor.RED));
                    return true;
                }
                boolean enabled = plugin.toggle(player);
                sender.sendMessage(Component.text("Days display " + (enabled ? "enabled" : "disabled") + ".",
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED));
            }
            default -> sender.sendMessage(Component.text("Usage: /" + label + " <reload|toggle>", NamedTextColor.YELLOW));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS;
        }
        return List.of();
    }
}
