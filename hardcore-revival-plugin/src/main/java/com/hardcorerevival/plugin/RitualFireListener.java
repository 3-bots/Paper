package com.hardcorerevival.plugin;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/** Handles right-clicking the ritual fire with a matching book to complete the revival. */
public final class RitualFireListener implements Listener {

    private final HardcoreRevivalPlugin plugin;

    public RitualFireListener(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        if (event.getClickedBlock().getType() != plugin.getFireMaterial()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!RitualBook.isBook(item)) {
            return;
        }

        Location fire = event.getClickedBlock().getLocation();
        if (!plugin.getAltarValidator().isValidAltar(fire)) {
            player.sendMessage(plugin.message("altar_invalid"));
            return;
        }

        String altarKey = AltarKeys.of(fire);
        long windowMillis = plugin.getSacrificeWindowSeconds() * 1000L;
        if (!plugin.getRitualState().hasRecentSacrifice(altarKey, windowMillis)) {
            player.sendMessage(plugin.message("sacrifice_needed"));
            return;
        }

        UUID targetId = plugin.getPendingRevivalRegistry().findMatchingName(RitualBook.extractText(item));
        if (targetId == null) {
            player.sendMessage(plugin.message("no_pending_player"));
            return;
        }

        Player target = plugin.getServer().getPlayer(targetId);
        if (target == null || !target.isOnline() || target.getGameMode() != GameMode.SPECTATOR) {
            player.sendMessage(plugin.message("target_not_online"));
            return;
        }

        item.setAmount(item.getAmount() - 1);
        plugin.getRitualState().clear(altarKey);
        plugin.getPendingRevivalRegistry().removePending(targetId);

        fire.getWorld().strikeLightningEffect(fire);
        target.setGameMode(GameMode.SURVIVAL);
        target.setHealth(target.getMaxHealth());
        target.teleport(fire.clone().add(0.5, 1, 0.5));
        target.sendMessage(plugin.message("revived_message"));

        plugin.getServer().broadcastMessage(plugin.message("revival_success", target.getName()));
        event.setCancelled(true);
    }
}
