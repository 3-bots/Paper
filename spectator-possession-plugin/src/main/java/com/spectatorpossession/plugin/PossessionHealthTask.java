package com.spectatorpossession.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/** Keeps the sidebar's health line current for every active possession. */
public final class PossessionHealthTask implements Runnable {

    private final SpectatorPossessionPlugin plugin;

    public PossessionHealthTask(SpectatorPossessionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Map.Entry<UUID, UUID> entry : plugin.getPossessionManager().getActivePossessions().entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }

            Entity entity = Bukkit.getEntity(entry.getValue());
            if (!(entity instanceof Mob mob) || !mob.isValid()) {
                continue;
            }

            AbilitySidebar.show(player, mob, plugin.getAbilityRegistry().getAbilities(mob.getType()));
        }
    }
}
