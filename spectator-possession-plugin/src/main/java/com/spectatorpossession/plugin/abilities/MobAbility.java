package com.spectatorpossession.plugin.abilities;

import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

/** A mob-specific action a possessing player can trigger, e.g. by sneaking. */
@FunctionalInterface
public interface MobAbility {

    void trigger(Player possessor, Mob mob);
}
