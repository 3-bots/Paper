package com.itemdurabilitydisplay.plugin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.function.Consumer;

public final class DurabilityDisplayTask implements Runnable {

    private final ItemDurabilityDisplayPlugin plugin;

    public DurabilityDisplayTask(ItemDurabilityDisplayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        PlayerInventory inv = player.getInventory();

        ItemStack[] storage = inv.getStorageContents();
        boolean storageChanged = false;
        for (ItemStack item : storage) {
            if (DurabilityLore.apply(item)) {
                storageChanged = true;
            }
        }
        if (storageChanged) {
            inv.setStorageContents(storage);
        }

        updateSlot(inv.getHelmet(), inv::setHelmet);
        updateSlot(inv.getChestplate(), inv::setChestplate);
        updateSlot(inv.getLeggings(), inv::setLeggings);
        updateSlot(inv.getBoots(), inv::setBoots);
        updateSlot(inv.getItemInOffHand(), inv::setItemInOffHand);
    }

    private void updateSlot(ItemStack item, Consumer<ItemStack> setter) {
        if (item != null && DurabilityLore.apply(item)) {
            setter.accept(item);
        }
    }
}
