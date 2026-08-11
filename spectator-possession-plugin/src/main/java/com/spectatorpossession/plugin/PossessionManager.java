package com.spectatorpossession.plugin;

import org.bukkit.Bukkit;
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
        // Deliberately not using Player#setSpectatorTarget here: vanilla's camera-lock
        // takes over input handling client-side, so the possessor's own WASD stops
        // producing PlayerMoveEvents entirely - they'd be a passive viewer with no
        // control, not a possessor. Instead, drop them right into the mob's eye spot
        // as a normal free-flying spectator; onMove() below then drags the mob along
        // with their real, unhijacked movement.
        player.teleport(mob.getEyeLocation());

        var abilities = plugin.getAbilityRegistry().getAbilities(mob.getType());
        // The book-open and scoreboard packets are unreliable if sent the same tick as
        // the teleport above - the client is still mid-processing the teleport
        // confirmation and silently drops them. Deferring by one tick is the standard
        // fix and makes both show up every time.
        UUID mobId = mob.getUniqueId();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!mobId.equals(getPossessedMobId(player)) || !mob.isValid()) {
                return;
            }
            player.openBook(ControlsBook.build(mob, abilities));
            AbilitySidebar.show(player, mob, abilities);
        });
    }

    /** Clears possession state, restores the mob's AI if it's still alive, and hides the player's sidebar. */
    public void stopPossessing(Player player) {
        UUID mobId = possessorToMob.remove(player.getUniqueId());
        if (mobId == null) {
            return;
        }
        mobToPossessor.remove(mobId);

        AbilitySidebar.hide(player);

        Entity entity = plugin.getServer().getEntity(mobId);
        if (entity instanceof Mob mob && mob.isValid()) {
            mob.setAI(true);
        }
    }

    /**
     * Releases the player no matter what state they're actually in - used by
     * /possess release and swap-hands. Normally this is just stopPossessing(), but
     * it also clears a lingering vanilla spectator-target camera lock even when our
     * own tracking has nothing on record for them (e.g. state left over from an
     * older build, a plugin reload mid-possession, or any other desync), so this
     * command is always a reliable "get me out of here" instead of sometimes
     * claiming "you aren't possessing anything" while the player is still stuck.
     * Returns true if anything was actually released.
     */
    public boolean forceRelease(Player player) {
        boolean released = isPossessing(player);
        if (released) {
            stopPossessing(player);
        }

        if (player.getSpectatorTarget() != null) {
            player.setSpectatorTarget(null);
            released = true;
        }

        return released;
    }
}
