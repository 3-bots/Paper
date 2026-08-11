package com.hardcorerevival.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.WritableBookMeta;

public final class RitualBook {

    private RitualBook() {
    }

    public static boolean isBook(ItemStack item) {
        if (item == null) {
            return false;
        }
        Material type = item.getType();
        return type == Material.WRITABLE_BOOK || type == Material.WRITTEN_BOOK;
    }

    /** Concatenates every page's text, for a simple contains() name check. */
    public static String extractText(ItemStack item) {
        if (!isBook(item)) {
            return "";
        }
        if (!(item.getItemMeta() instanceof WritableBookMeta meta) || !meta.hasPages()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String page : meta.getPages()) {
            builder.append(page).append(' ');
        }
        return builder.toString();
    }

    public static void markGlowingRed(ItemStack item, String targetName) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.setDisplayName(ChatColor.RED + "Ritual of " + targetName);
        item.setItemMeta(meta);
    }
}
