package com.hardcorerevival.plugin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Periodically renames a held ritual book red once its name and altar proximity
 * both check out, and flips on a fake "the sky is red" warning border for that
 * player while it holds, clearing it back to normal the moment it doesn't.
 */
public final class BookGlowTask implements Runnable {

    private final HardcoreRevivalPlugin plugin;
    private final Set<UUID> redSkyActive = new HashSet<>();

    public BookGlowTask(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        redSkyActive.removeIf(uuid -> plugin.getServer().getPlayer(uuid) == null);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isRitualReady(player)) {
                if (redSkyActive.add(player.getUniqueId())) {
                    RedSkyEffect.apply(player);
                }
            } else if (redSkyActive.remove(player.getUniqueId())) {
                RedSkyEffect.clear(player);
            }
        }
    }

    /** True if this player is holding a book naming a pending player, near that player's complete altar. */
    private boolean isRitualReady(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!RitualBook.isBook(item)) {
            return false;
        }

        UUID targetId = plugin.getPendingRevivalRegistry().findMatchingName(RitualBook.extractText(item));
        if (targetId == null) {
            return false;
        }

        if (plugin.getAltarFinder().findNearbyAltarFire(player.getLocation()) == null) {
            return false;
        }

        RitualBook.markGlowingRed(item, plugin.getPendingRevivalRegistry().nameOf(targetId));
        return true;
    }

    /** Clears the red-sky effect for everyone currently affected - call this on plugin disable/restart. */
    public void clearAllRedSky() {
        for (UUID uuid : redSkyActive) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                RedSkyEffect.clear(player);
            }
        }
        redSkyActive.clear();
    }
}
