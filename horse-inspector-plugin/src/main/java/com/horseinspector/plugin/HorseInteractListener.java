package com.horseinspector.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.UUID;

public final class HorseInteractListener implements Listener {

    private final HorseInspectorPlugin plugin;
    private final InspectorTool tool;

    public HorseInteractListener(HorseInspectorPlugin plugin, InspectorTool tool) {
        this.plugin = plugin;
        this.tool = tool;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof AbstractHorse horse)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (!tool.isTool(handItem)) {
            return;
        }

        event.setCancelled(true);

        if (player.isSneaking()) {
            handleSaddleRemoval(player, horse);
            return;
        }

        handleInspect(player, horse);
    }

    private void handleSaddleRemoval(Player player, AbstractHorse horse) {
        ItemStack saddle = horse.getInventory().getSaddle();
        if (saddle == null || saddle.getType() == Material.AIR) {
            player.sendMessage(plugin.prefixed("&fThat horse isn't saddled."));
            return;
        }

        horse.getInventory().setSaddle(null);
        var leftover = player.getInventory().addItem(saddle);
        for (ItemStack overflow : leftover.values()) {
            horse.getWorld().dropItemNaturally(horse.getLocation(), overflow);
        }
        player.sendMessage(plugin.prefixed("&fRemoved the saddle."));
    }

    private void handleInspect(Player player, AbstractHorse horse) {
        HorseStats stats = readStats(horse);
        sendStats(player, describe(horse), stats);

        HorseInspectorPlugin.Selection selection = plugin.getSelection(player.getUniqueId());
        if (selection == null) {
            plugin.setSelection(player.getUniqueId(), horse.getUniqueId());
            return;
        }

        if (selection.horseId().equals(horse.getUniqueId())) {
            // Clicked the same horse again; keep it selected, nothing more to do.
            return;
        }

        Entity previousEntity = plugin.getServer().getEntity(selection.horseId());
        plugin.clearSelection(player.getUniqueId());

        if (!(previousEntity instanceof AbstractHorse previousHorse) || !previousHorse.isValid()) {
            player.sendMessage(plugin.prefixed("&7(Your previously inspected horse is no longer around.)"));
            plugin.setSelection(player.getUniqueId(), horse.getUniqueId());
            return;
        }

        if (previousHorse.getClass() != horse.getClass()) {
            player.sendMessage(plugin.prefixed("&7These are different horse types, so no breeding prediction."));
            plugin.setSelection(player.getUniqueId(), horse.getUniqueId());
            return;
        }

        HorseStats previousStats = readStats(previousHorse);
        sendBreedingPrediction(player, previousStats, stats);
    }

    private String describe(AbstractHorse horse) {
        String name = horse.getCustomName() != null ? horse.getCustomName() : horse.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String color = horse instanceof Horse h ? " (" + h.getColor().name().toLowerCase(Locale.ROOT).replace('_', ' ') + ")" : "";
        return name + color;
    }

    private HorseStats readStats(AbstractHorse horse) {
        double health = attr(horse, Attribute.GENERIC_MAX_HEALTH, plugin.getHealthMin());
        double speed = attr(horse, Attribute.GENERIC_MOVEMENT_SPEED, plugin.getSpeedMin());
        double jump = attr(horse, Attribute.HORSE_JUMP_STRENGTH, plugin.getJumpMin());
        return new HorseStats(health, speed, jump);
    }

    private double attr(AbstractHorse horse, Attribute attribute, double fallback) {
        var instance = horse.getAttribute(attribute);
        return instance != null ? instance.getValue() : fallback;
    }

    private void sendStats(Player player, String label, HorseStats stats) {
        player.sendMessage(plugin.prefixed("&fStats for &e" + label + "&f:"));
        player.sendMessage(statLine("Health", stats.health() / 2.0, " hearts", stats.health(), plugin.getHealthMin(), plugin.getHealthMax()));
        player.sendMessage(statLine("Speed", approxBlocksPerSecond(stats.speed()), " blocks/s (approx)", stats.speed(), plugin.getSpeedMin(), plugin.getSpeedMax()));
        player.sendMessage(statLine("Jump", approxJumpHeight(stats.jump()), " blocks (approx)", stats.jump(), plugin.getJumpMin(), plugin.getJumpMax()));
    }

    private String statLine(String name, double displayValue, String unit, double raw, double min, double max) {
        double percent = max > min ? ((raw - min) / (max - min)) * 100.0 : 0.0;
        return String.format("%s &7- &f%.2f%s &7(%.0f%% of max, raw %.4f)", ChatColor.GRAY + " " + name + ":", displayValue, unit, percent, raw);
    }

    private void sendBreedingPrediction(Player player, HorseStats a, HorseStats b) {
        HorseStats avg = HorseStats.averageOf(a, b);
        double variance = plugin.getVarianceFraction();

        double healthSpread = (plugin.getHealthMax() - plugin.getHealthMin()) * variance;
        double speedSpread = (plugin.getSpeedMax() - plugin.getSpeedMin()) * variance;
        double jumpSpread = (plugin.getJumpMax() - plugin.getJumpMin()) * variance;

        player.sendMessage(plugin.prefixed("&fPredicted foal stats &7(90% chance around this, 10% chance of a fresh random roll):"));
        player.sendMessage(rangeLine("Health", (avg.health() - healthSpread) / 2.0, (avg.health() + healthSpread) / 2.0, " hearts"));
        player.sendMessage(rangeLine("Speed", approxBlocksPerSecond(avg.speed() - speedSpread), approxBlocksPerSecond(avg.speed() + speedSpread), " blocks/s"));
        player.sendMessage(rangeLine("Jump", approxJumpHeight(Math.max(plugin.getJumpMin(), avg.jump() - jumpSpread)), approxJumpHeight(Math.min(plugin.getJumpMax(), avg.jump() + jumpSpread)), " blocks"));
    }

    private String rangeLine(String name, double low, double high, String unit) {
        return String.format("%s &7- &f%.2f&7-&f%.2f%s", ChatColor.GRAY + " " + name + ":", Math.min(low, high), Math.max(low, high), unit);
    }

    private double approxBlocksPerSecond(double speedAttribute) {
        return speedAttribute * 43.0;
    }

    private double approxJumpHeight(double jumpAttribute) {
        double x = jumpAttribute;
        return -0.1817 * Math.pow(x, 3) + 3.689 * Math.pow(x, 2) + 2.128 * x - 0.343;
    }
}
