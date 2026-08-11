package com.hardcorerevival.plugin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/** Periodically renames a held ritual book red once its name and altar proximity both check out. */
public final class BookGlowTask implements Runnable {

    private final HardcoreRevivalPlugin plugin;

    public BookGlowTask(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!RitualBook.isBook(item)) {
                continue;
            }

            UUID targetId = plugin.getPendingRevivalRegistry().findMatchingName(RitualBook.extractText(item));
            if (targetId == null) {
                continue;
            }

            if (plugin.getAltarFinder().findNearbyAltarFire(player.getLocation()) == null) {
                continue;
            }

            RitualBook.markGlowingRed(item, plugin.getPendingRevivalRegistry().nameOf(targetId));
        }
    }
}
