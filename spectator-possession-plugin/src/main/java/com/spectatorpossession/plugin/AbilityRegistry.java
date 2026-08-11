package com.spectatorpossession.plugin;

import com.spectatorpossession.plugin.abilities.CreeperExplodeAbility;
import com.spectatorpossession.plugin.abilities.MobAbility;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Per-mob-type abilities, each bound to a hotbar key (1-4). Add more via register() to extend it. */
public final class AbilityRegistry {

    public record AbilityDefinition(int slot, String name, String description, MobAbility action) {
    }

    private final Map<EntityType, List<AbilityDefinition>> abilities = new EnumMap<>(EntityType.class);

    public AbilityRegistry(SpectatorPossessionPlugin plugin) {
        register(EntityType.CREEPER, 1, "Explode", "Detonates immediately.", new CreeperExplodeAbility(plugin));
    }

    private void register(EntityType type, int slot, String name, String description, MobAbility action) {
        abilities.computeIfAbsent(type, key -> new ArrayList<>())
                .add(new AbilityDefinition(slot, name, description, action));
    }

    public List<AbilityDefinition> getAbilities(EntityType type) {
        return abilities.getOrDefault(type, List.of());
    }

    public AbilityDefinition getAbility(EntityType type, int slot) {
        for (AbilityDefinition definition : getAbilities(type)) {
            if (definition.slot() == slot) {
                return definition;
            }
        }
        return null;
    }
}
