package com.spectatorpossession.plugin.abilities;

import com.spectatorpossession.plugin.SpectatorPossessionPlugin;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public final class CreeperExplodeAbility implements MobAbility {

    private static final float EXPLOSION_POWER = 3.0f;

    private final SpectatorPossessionPlugin plugin;

    public CreeperExplodeAbility(SpectatorPossessionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void trigger(Player possessor, Mob mob) {
        if (!(mob instanceof Creeper creeper)) {
            return;
        }
        creeper.getWorld().createExplosion(creeper.getLocation(), EXPLOSION_POWER, false, plugin.isExplosionBreaksBlocks());
        creeper.remove();

        // Entity#remove() doesn't fire EntityDeathEvent, so release explicitly
        // instead of leaving the possessor stuck pointed at a gone entity.
        plugin.getPossessionManager().stopPossessing(possessor);
        possessor.sendMessage(plugin.message("possess_ended_death"));
    }
}
