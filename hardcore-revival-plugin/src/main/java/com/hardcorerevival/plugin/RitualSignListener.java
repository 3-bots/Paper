package com.hardcorerevival.plugin;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.UUID;

/**
 * The moment a player signs a book naming someone pending, tells them in chat
 * exactly what's still missing for the ritual to work - or that it's ready.
 * Says nothing for a signed book that doesn't name anyone pending, so signing
 * an unrelated book stays silent.
 */
public final class RitualSignListener implements Listener {

    private final HardcoreRevivalPlugin plugin;

    public RitualSignListener(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSign(PlayerEditBookEvent event) {
        if (!event.isSigning()) {
            return;
        }

        String text = extractText(event.getNewBookMeta());
        UUID targetId = plugin.getPendingRevivalRegistry().findMatchingName(text);
        if (targetId == null) {
            return;
        }

        Player player = event.getPlayer();
        String targetName = plugin.getPendingRevivalRegistry().nameOf(targetId);

        Location fire = plugin.getAltarFinder().findNearbyAltarFire(player.getLocation());
        if (fire == null) {
            player.sendMessage(plugin.message("sign_feedback_no_altar", targetName));
            return;
        }

        String altarKey = AltarKeys.of(fire);
        long windowMillis = plugin.getSacrificeWindowSeconds() * 1000L;
        if (!plugin.getRitualState().hasRecentSacrifice(altarKey, windowMillis)) {
            player.sendMessage(plugin.message("sign_feedback_no_sacrifice", targetName));
            return;
        }

        Player target = plugin.getServer().getPlayer(targetId);
        if (target == null || !target.isOnline() || target.getGameMode() != GameMode.SPECTATOR) {
            player.sendMessage(plugin.message("sign_feedback_target_offline", targetName));
            return;
        }

        player.sendMessage(plugin.message("sign_feedback_ready", targetName));
    }

    private static String extractText(BookMeta meta) {
        if (!meta.hasPages()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String page : meta.getPages()) {
            builder.append(page).append(' ');
        }
        return builder.toString();
    }
}
