package com.spectatorpossession.plugin;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
     * built-in trigger for camera-locking onto it (Player#setSpectatorTarget) - it
     * doesn't go through PlayerInteractEntityEvent or EntityDamageByEntityEvent at
     * all, which is why both of those were unreliable triggers for possession.
     * This Paper-specific event fires right as that vanilla camera-lock is about
     * to happen and is cancellable, so this intercepts it: not currently
     * possessing anything -> start possessing whatever was clicked; already
     * possessing something -> the click is instead treated as that mob attacking
     * whatever was clicked, since a possessor has no other way to deal damage
     * (spectators can't attack at all - that's blocked before any Bukkit damage
     * event would even fire).
     */
    @EventHandler(ignoreCancelled = true)
    public void onStartSpectating(PlayerStartSpectatingEntityEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        UUID possessedMobId = plugin.getPossessionManager().getPossessedMobId(player);
        if (possessedMobId != null) {
            event.setCancelled(true);
            attackWithPossessedMob(possessedMobId, event.getNewSpectatorTarget());
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

    private void attackWithPossessedMob(UUID possessedMobId, Entity clicked) {
        if (clicked.getUniqueId().equals(possessedMobId) || !(clicked instanceof LivingEntity target)) {
            return;
        }

        Entity possessedEntity = plugin.getServer().getEntity(possessedMobId);
        if (!(possessedEntity instanceof Mob mob) || !mob.isValid()) {
            return;
        }

        AttributeInstance attackAttribute = mob.getAttribute(Attribute.ATTACK_DAMAGE);
        double damage = attackAttribute != null ? attackAttribute.getValue() : 1.0;
        target.damage(damage, mob);
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
            // Vanilla auto-releases the camera-lock the instant the possessor moves -
            // re-applying it here every time it's slipped keeps the through-its-eyes
            // view effectively permanent without ever blocking movement.
            if (!mob.equals(player.getSpectatorTarget())) {
                player.setSpectatorTarget(mob);
            }
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

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() == GameMode.SPECTATOR) {
            return;
        }
        Player player = event.getPlayer();
        if (plugin.getPossessionManager().isPossessing(player)) {
            plugin.getPossessionManager().stopPossessing(player);
        }
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
