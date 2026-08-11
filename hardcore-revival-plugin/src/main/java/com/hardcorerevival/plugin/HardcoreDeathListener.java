package com.hardcorerevival.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class HardcoreDeathListener implements Listener {

    private final HardcoreRevivalPlugin plugin;

    public HardcoreDeathListener(HardcoreRevivalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.getWorld().isHardcore()) {
            return;
        }
        plugin.getPendingRevivalRegistry().addPending(player.getUniqueId(), player.getName());
    }
}
