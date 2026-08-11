package com.hardcorerevival.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.WritableBookMeta;

import java.util.List;

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

        // BookMeta (WRITTEN_BOOK, i.e. a signed book) formally extends WritableBookMeta
        // in the API's type hierarchy, but the API's own javadoc warns instanceof against
        // the parent interface is not reliable for telling them apart - the concrete
        // implementations don't actually share that relationship. Branching on the
        // material first and casting to the exact matching type is what the javadoc
        // itself recommends, and is the only way a signed book's pages get read at all.
        ItemMeta meta = item.getItemMeta();
        List<String> pages;
        if (item.getType() == Material.WRITTEN_BOOK) {
            if (!(meta instanceof BookMeta bookMeta) || !bookMeta.hasPages()) {
                return "";
            }
            pages = bookMeta.getPages();
        } else {
            if (!(meta instanceof WritableBookMeta writableMeta) || !writableMeta.hasPages()) {
                return "";
            }
            pages = writableMeta.getPages();
        }

        StringBuilder builder = new StringBuilder();
        for (String page : pages) {
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
