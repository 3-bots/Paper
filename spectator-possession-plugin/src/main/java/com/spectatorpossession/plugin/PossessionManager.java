package com.spectatorpossession.plugin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PossessionManager {

    private final SpectatorPossessionPlugin plugin;
    private final Map<UUID, UUID> possessorToMob = new HashMap<>();
    private final Map<UUID, UUID> mobToPossessor = new HashMap<>();

    public PossessionManager(SpectatorPossessionPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isPossessing(Player player) {
        return possessorToMob.containsKey(player.getUniqueId());
    }

    public boolean isPossessed(Mob mob) {
        return mobToPossessor.containsKey(mob.getUniqueId());
    }

    public UUID getPossessedMobId(Player player) {
        return possessorToMob.get(player.getUniqueId());
    }

    public UUID getPossessorId(Mob mob) {
        return mobToPossessor.get(mob.getUniqueId());
    }

    public void startPossessing(Player player, Mob mob) {
        possessorToMob.put(player.getUniqueId(), mob.getUniqueId());
        mobToPossessor.put(mob.getUniqueId(), player.getUniqueId());

        mob.setAI(false);
        player.setSpectatorTarget(mob);

        var abilities = plugin.getAbilityRegistry().getAbilities(mob.getType());
        player.openBook(ControlsBook.build(mob, abilities));
        AbilitySidebar.show(player, mob, abilities);
    }

    /** Clears possession state, restores the mob's AI if it's still alive, and resets the player's camera/sidebar. */
    public void stopPossessing(Player player) {
        UUID mobId = possessorToMob.remove(player.getUniqueId());
        if (mobId == null) {
            return;
        }
        mobToPossessor.remove(mobId);

        if (player.getSpectatorTarget() != null) {
            player.setSpectatorTarget(null);
        }
        AbilitySidebar.hide(player);

        Entity entity = plugin.getServer().getEntity(mobId);
        if (entity instanceof Mob mob && mob.isValid()) {
            mob.setAI(true);
        }
    }
}
