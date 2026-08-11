package com.spectatorpossession.plugin;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
    private final Map<UUID, GameMode> previousGameMode = new HashMap<>();

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

    /**
     * Disguise approach, same idea real morph/disguise plugins use, instead of
     * vanilla's Player#setSpectatorTarget camera-lock - that turned out too
     * unreliable in practice to depend on (see README history). The possessor
     * becomes an ordinary, invisible Creative-mode player: native flight, hearts,
     * hunger, movement and combat, none of it hacked. The mob is just a "costume"
     * kept mirrored to wherever the possessor actually is.
     */
    public void startPossessing(Player player, Mob mob) {
        possessorToMob.put(player.getUniqueId(), mob.getUniqueId());
        mobToPossessor.put(mob.getUniqueId(), player.getUniqueId());
        previousGameMode.put(player.getUniqueId(), player.getGameMode());

        mob.setAI(false);
        mob.teleport(player.getLocation());

        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvisible(true);

        // Sent immediately, so it's a reliable in-game way to confirm which jar
        // version is actually running - useful for ruling out a stale build.
        player.sendMessage(plugin.message("possess_started", plugin.getDescription().getVersion()));

        var abilities = plugin.getAbilityRegistry().getAbilities(mob.getType());
        UUID mobId = mob.getUniqueId();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!mobId.equals(getPossessedMobId(player)) || !mob.isValid()) {
                return;
            }
            player.openBook(ControlsBook.build(mob, abilities));
            AbilitySidebar.show(player, mob, abilities);
        });
    }

    /** Clears possession state, restores the mob's AI, and puts the player back the way they were. */
    public void stopPossessing(Player player) {
        UUID mobId = possessorToMob.remove(player.getUniqueId());
        if (mobId == null) {
            return;
        }
        mobToPossessor.remove(mobId);

        AbilitySidebar.hide(player);

        player.setInvisible(false);
        GameMode restoreTo = previousGameMode.remove(player.getUniqueId());
        if (restoreTo != null) {
            player.setGameMode(restoreTo);
        }
        if (restoreTo != GameMode.CREATIVE && restoreTo != GameMode.SPECTATOR) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        Entity entity = plugin.getServer().getEntity(mobId);
        if (entity instanceof Mob mob && mob.isValid()) {
            mob.setAI(true);
        }
    }

    /**
     * Releases the player no matter what state they're actually in - used by
     * /possess release and swap-hands, so it's always a reliable "get me out of
     * here" instead of sometimes claiming "you aren't possessing anything" while
     * the player is still stuck. Returns true if anything was actually released.
     */
    public boolean forceRelease(Player player) {
        boolean released = isPossessing(player);
        if (released) {
            stopPossessing(player);
        }

        // No lingering vanilla spectator-target lock to worry about with this
        // approach, but clear one defensively anyway in case state from an older
        // build (which did use it) is still hanging around.
        if (player.getSpectatorTarget() != null) {
            player.setSpectatorTarget(null);
            released = true;
        }

        return released;
    }
}
