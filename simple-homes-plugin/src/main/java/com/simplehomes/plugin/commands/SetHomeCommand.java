package com.simplehomes.plugin.commands;

import com.simplehomes.plugin.CostEntry;
import com.simplehomes.plugin.SimpleHomesPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class SetHomeCommand implements CommandExecutor {

    private final SimpleHomesPlugin plugin;

    public SetHomeCommand(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.message("player_only"));
            return true;
        }

        if (!player.hasPermission("simplehomes.sethome")) {
            player.sendMessage(plugin.message("no_permission"));
            return true;
        }

        boolean explicitName = args.length > 0;
        String name = explicitName ? args[0].toLowerCase() : SimpleHomesPlugin.DEFAULT_HOME_NAME;

        if (explicitName && name.equals(SimpleHomesPlugin.DEFAULT_HOME_NAME)) {
            player.sendMessage(plugin.message("reserved_name"));
            return true;
        }

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());

        if (homes.containsKey(name)) {
            if (name.equals(SimpleHomesPlugin.DEFAULT_HOME_NAME)) {
                player.sendMessage(plugin.message("default_home_exists"));
            } else {
                player.sendMessage(plugin.message("home_name_exists", args[0]));
            }
            return true;
        }

        if (homes.size() >= plugin.getMaxHomes()) {
            player.sendMessage(plugin.message("max_homes_reached", plugin.getMaxHomes()));
            return true;
        }

        int nextSlot = homes.size() + 1;
        boolean bypass = player.hasPermission("simplehomes.bypasscost");
        List<CostEntry> required = bypass ? List.of() : plugin.getCostFor(nextSlot);

        for (CostEntry entry : required) {
            if (!hasEnough(player, entry)) {
                player.sendMessage(plugin.message("insufficient_resources", plugin.describeCosts(required)));
                return true;
            }
        }

        for (CostEntry entry : required) {
            player.getInventory().removeItem(new ItemStack(entry.material(), entry.amount()));
        }
        if (!required.isEmpty()) {
            player.sendMessage(plugin.message("home_cost_charged", plugin.describeCosts(required)));
        }

        homes.put(name, player.getLocation());
        plugin.getHomeManager().saveHomes(player.getUniqueId());

        if (name.equals(SimpleHomesPlugin.DEFAULT_HOME_NAME)) {
            player.sendMessage(plugin.message("default_home_set"));
        } else {
            player.sendMessage(plugin.message("custom_home_set", args[0]));
        }
        return true;
    }

    private boolean hasEnough(Player player, CostEntry entry) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == entry.material()) {
                count += stack.getAmount();
            }
        }
        return count >= entry.amount();
    }
}
