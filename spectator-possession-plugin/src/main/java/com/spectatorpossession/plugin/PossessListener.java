package com.spectatorpossession.plugin;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.UUID;

public final class PossessListener implements Listener {

    private final SpectatorPossessionPlugin plugin;

    public PossessListener(SpectatorPossessionPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Left-clicking (attacking) an entity while in spectator mode is vanilla's own
     * built-in trigger for camera-locking onto it - it doesn't go through
     * PlayerInteractEntityEvent or EntityDamageByEntityEvent at all, which is why
     * both of those were unreliable triggers for possession. This Paper-specific
     * event fires right as that vanilla camera-lock is about to happen and is
     * cancellable, so this intercepts it to start possessing whatever was clicked.
     */
    @EventHandler(ignoreCancelled = true)
    public void onStartSpectating(PlayerStartSpectatingEntityEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        if (!(event.getNewSpectatorTarget() instanceof Mob mob)) {
            return;
        }

        event.setCancelled(true);

        if (plugin.getPossessionManager().isPossessed(mob)) {
            player.sendMessage(plugin.message("possess_denied_taken"));
            return;
        }

        plugin.getPossessionManager().startPossessing(player, mob);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID mobId = plugin.getPossessionManager().getPossessedMobId(player);
        if (mobId == null || event.getTo() == null) {
            return;
        }

        Entity entity = plugin.getServer().getEntity(mobId);
        if (entity instanceof Mob mob && mob.isValid()) {
            mob.teleport(event.getTo());
        }
    }

    /** The possessor's own combat is real (they're an ordinary Creative player now), just corrected to hit like the mob would. */
    @EventHandler(ignoreCancelled = true)
    public void onPossessorAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        UUID mobId = plugin.getPossessionManager().getPossessedMobId(player);
        if (mobId == null) {
            return;
        }

        Entity entity = plugin.getServer().getEntity(mobId);
        if (!(entity instanceof Mob mob) || !mob.isValid()) {
            return;
        }

        AttributeInstance attackAttribute = mob.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackAttribute != null) {
            event.setDamage(attackAttribute.getValue());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID mobId = plugin.getPossessionManager().getPossessedMobId(player);
        if (mobId == null) {
            return;
        }

        Entity entity = plugin.getServer().getEntity(mobId);
        if (!(entity instanceof Mob mob) || !mob.isValid()) {
            return;
        }

        int pressedKey = event.getNewSlot() + 1;
        var ability = plugin.getAbilityRegistry().getAbility(mob.getType(), pressedKey);
        if (ability == null) {
            return;
        }
        ability.action().trigger(player, mob);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPossessionManager().forceRelease(player)) {
            return;
        }
        player.sendMessage(plugin.message("possess_released"));
        event.setCancelled(true);
    }

    /**
     * Possessing itself switches the player to Creative (see PossessionManager),
     * which would otherwise immediately trip this same check - only treat it as
     * the possessor manually/externally leaving (e.g. an op running /gamemode)
     * if the new mode isn't the Creative we just set them to.
     */
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPossessionManager().isPossessing(player)) {
            return;
        }
        if (event.getNewGameMode() == GameMode.CREATIVE) {
            return;
        }
        plugin.getPossessionManager().stopPossessing(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getPossessionManager().isPossessing(player)) {
            plugin.getPossessionManager().stopPossessing(player);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }
        UUID possessorId = plugin.getPossessionManager().getPossessorId(mob);
        if (possessorId == null) {
            return;
        }
        Player possessor = plugin.getServer().getPlayer(possessorId);
        if (possessor != null) {
            plugin.getPossessionManager().stopPossessing(possessor);
            possessor.sendMessage(plugin.message("possess_ended_death"));
        }
    }
}
