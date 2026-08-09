package com.itemdurabilitydisplay.plugin;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class DurabilityLore {

    private static final String PREFIX = ChatColor.GRAY + "Durability: ";

    private DurabilityLore() {
    }

    /** Adds or refreshes the durability tooltip line on an item. Returns true if the item was changed. */
    public static boolean apply(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return false;
        }

        int maxDurability = damageable.hasMaxDamage() ? damageable.getMaxDamage() : item.getType().getMaxDurability();
        if (maxDurability <= 0) {
            return false;
        }

        String line;
        if (meta.isUnbreakable()) {
            line = PREFIX + ChatColor.AQUA + "Unbreakable";
        } else {
            int current = maxDurability - damageable.getDamage();
            double ratio = (double) current / maxDurability;
            ChatColor valueColor = ratio > 0.5 ? ChatColor.GREEN : ratio > 0.2 ? ChatColor.YELLOW : ChatColor.RED;
            line = PREFIX + valueColor + current + ChatColor.GRAY + " / " + valueColor + maxDurability;
        }

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        int existingIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).startsWith(PREFIX)) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex >= 0) {
            if (line.equals(lore.get(existingIndex))) {
                return false;
            }
            lore.set(existingIndex, line);
        } else {
            lore.add(line);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return true;
    }
}
