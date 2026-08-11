package com.spectatorpossession.plugin;

import com.spectatorpossession.plugin.abilities.CreeperExplodeAbility;
import com.spectatorpossession.plugin.abilities.MobAbility;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.Map;

/** Per-mob-type abilities a possessing player can trigger. Add more entries here to extend it. */
public final class AbilityRegistry {

    private final Map<EntityType, MobAbility> abilities = new EnumMap<>(EntityType.class);

    public AbilityRegistry(SpectatorPossessionPlugin plugin) {
        abilities.put(EntityType.CREEPER, new CreeperExplodeAbility(plugin));
    }

    public MobAbility get(EntityType type) {
        return abilities.get(type);
    }
}
