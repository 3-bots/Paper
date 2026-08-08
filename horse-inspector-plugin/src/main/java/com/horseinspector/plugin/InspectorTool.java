package com.horseinspector.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class InspectorTool {

    private final NamespacedKey key;

    public InspectorTool(HorseInspectorPlugin plugin) {
        this.key = new NamespacedKey(plugin, "horse_inspector_tool");
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Horse Inspector");
        meta.setLore(List.of(
                ChatColor.GRAY + "Right-click a horse to inspect its stats",
                ChatColor.GRAY + "Right-click a second horse to compare a breeding prediction",
                ChatColor.GRAY + "Shift+right-click a saddled horse to remove its saddle"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public void registerRecipe(HorseInspectorPlugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(key, createItem());
        recipe.shape("G G", " S ", "G G");
        recipe.setIngredient('G', Material.GOLD_NUGGET);
        recipe.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(recipe);
    }

    public void unregisterRecipe(HorseInspectorPlugin plugin) {
        plugin.getServer().removeRecipe(key);
    }
}
