package com.spectatorpossession.plugin;

import org.bukkit.GameMode;
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

    @EventHandler(ignoreCancelled = true)
    public void onAttackEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        event.setCancelled(true);

        if (plugin.getPossessionManager().isPossessing(player)) {
            return;
        }
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
