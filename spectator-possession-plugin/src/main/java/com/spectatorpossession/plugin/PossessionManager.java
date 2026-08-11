package com.spectatorpossession.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.Collections;
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

    /** Possessor UUID -> possessed mob UUID, for iterating every active possession (e.g. the health-refresh task). */
    public Map<UUID, UUID> getActivePossessions() {
        return Collections.unmodifiableMap(possessorToMob);
    }

    public void startPossessing(Player player, Mob mob) {
        possessorToMob.put(player.getUniqueId(), mob.getUniqueId());
        mobToPossessor.put(mob.getUniqueId(), player.getUniqueId());

        mob.setAI(false);
        // Real vanilla behavior: Player#setSpectatorTarget's camera-lock auto-releases
        // the instant the possessor moves - it's not that WASD is disabled, it's that
        // moving un-attaches the camera and reverts to free spectating. Rather than
        // avoid the lock (losing the true through-its-eyes view), onMove() below keeps
        // re-applying it every time it breaks, while still using the possessor's real,
        // never-actually-blocked movement to drag the mob along. Teleporting them into
        // the mob's eye spot first gets the initial view aligned before that kicks in.
        player.teleport(mob.getEyeLocation());

        // Sent immediately (not deferred), independent of the book/sidebar below, so
        // it's a reliable in-game way to confirm which jar version is actually
        // running - useful for ruling out a stale build when troubleshooting.
        player.sendMessage(plugin.message("possess_started", plugin.getDescription().getVersion()));

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
            player.setSpectatorTarget(mob);
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
