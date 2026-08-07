package com.sugarcanebonemeal.plugin;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class DispenserBonemealListener implements Listener {

    private final SugarCaneBonemealPlugin plugin;

    public DispenserBonemealListener(SugarCaneBonemealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        Block dispenserBlock = event.getBlock();
        if (dispenserBlock.getType() != Material.DISPENSER) {
            return;
        }
        if (event.getItem().getType() != Material.BONE_MEAL) {
            return;
        }

        BlockData data = dispenserBlock.getBlockData();
        if (!(data instanceof Directional directional)) {
            return;
        }

        Block target = dispenserBlock.getRelative(directional.getFacing());
        if (target.getType() != Material.SUGAR_CANE) {
            return;
        }

        Block top = target;
        while (top.getRelative(BlockFace.UP).getType() == Material.SUGAR_CANE) {
            top = top.getRelative(BlockFace.UP);
        }

        Block base = target;
        while (base.getRelative(BlockFace.DOWN).getType() == Material.SUGAR_CANE) {
            base = base.getRelative(BlockFace.DOWN);
        }

        int height = top.getY() - base.getY() + 1;
        if (height >= plugin.getMaxHeight()) {
            return;
        }

        Block above = top.getRelative(BlockFace.UP);
        if (above.getType() != Material.AIR && above.getType() != Material.CAVE_AIR) {
            return;
        }

        event.setCancelled(true);
        above.setType(Material.SUGAR_CANE);
        consumeBoneMeal(dispenserBlock);

        if (plugin.isPlayEffectsEnabled()) {
            Location effectLocation = top.getLocation().add(0.5, 0.5, 0.5);
            top.getWorld().playEffect(top.getLocation(), Effect.BONE_MEAL_USE, 30);
            top.getWorld().playSound(effectLocation, Sound.ITEM_BONE_MEAL_USE, 1f, 1f);
        }
    }

    private void consumeBoneMeal(Block dispenserBlock) {
        if (!(dispenserBlock.getState() instanceof Dispenser dispenser)) {
            return;
        }
        Inventory inventory = dispenser.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null && stack.getType() == Material.BONE_MEAL) {
                if (stack.getAmount() <= 1) {
                    inventory.setItem(i, null);
                } else {
                    stack.setAmount(stack.getAmount() - 1);
                }
                break;
            }
        }
    }
}
