package com.spectatorpossession.plugin;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.UUID;

public final class PossessListener implements Listener {

    private static final double MOVE_RESPONSIVENESS = 1.2;
    private static final double MAX_STEP = 2.0;
    private static final double ATTACK_RANGE = 4.0;

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
     * to happen and is cancellable, so this intercepts it to start possessing
     * whatever was clicked.
     */
    @EventHandler(ignoreCancelled = true)
    public void onStartSpectating(PlayerStartSpectatingEntityEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        if (plugin.getPossessionManager().isPossessing(player)) {
            // Already possessing - don't let vanilla (or onMove's own repeated
            // re-locking below, which also fires this event) switch the camera
            // target away from the possessed mob.
            event.setCancelled(true);
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

    /**
     * Arm-swing fires on every left-click regardless of gamemode or what (if
     * anything) is being targeted, unlike PlayerStartSpectatingEntityEvent which
     * likely only fires on the initial lock-on and not for clicks after that -
     * making this the reliable way to detect "the possessor wants to attack".
     * Raytraces from the mob's own eyes (not the possessor's) since that's what's
     * actually rendered on screen and is kept facing wherever the possessor looks.
     */
    @EventHandler(ignoreCancelled = true)
    public void onSwingArm(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }

        Player player = event.getPlayer();
        UUID mobId = plugin.getPossessionManager().getPossessedMobId(player);
        if (mobId == null) {
            return;
        }

        Entity entity = plugin.getServer().getEntity(mobId);
        if (!(entity instanceof Mob mob) || !mob.isValid()) {
            return;
        }

        Location eye = mob.getEyeLocation();
        RayTraceResult hit = mob.getWorld().rayTraceEntities(eye, eye.getDirection(), ATTACK_RANGE, candidate -> candidate != mob);
        if (hit == null || !(hit.getHitEntity() instanceof LivingEntity target)) {
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
        Location from = event.getFrom();
        Location to = event.getTo();
        if (mobId == null || to == null) {
            return;
        }

        Entity entity = plugin.getServer().getEntity(mobId);
        if (!(entity instanceof Mob mob) || !mob.isValid()) {
            return;
        }

        // Velocity instead of teleporting the mob to the possessor's exact position:
        // teleporting let it "fly" anywhere regardless of what it actually is (a
        // chicken soaring through the sky), and repeatedly teleporting the entity
        // the camera is locked to causes visible hitches. Velocity respects each
        // mob's own natural physics - gravity keeps a chicken grounded while a
        // naturally airborne mob stays aloft - and moves smoothly like normal mob
        // movement instead of snapping every tick.
        Vector delta = to.toVector().subtract(from.toVector());
        if (delta.lengthSquared() > MAX_STEP * MAX_STEP) {
            delta = delta.normalize().multiply(MAX_STEP);
        }
        if (delta.lengthSquared() > 0) {
            mob.setVelocity(delta.multiply(MOVE_RESPONSIVENESS));
        }
        mob.setRotation(to.getYaw(), to.getPitch());

        // Vanilla auto-releases the camera-lock the instant the possessor moves -
        // re-applying it here every time it's slipped keeps the through-its-eyes
        // view effectively permanent without ever blocking movement.
        if (!mob.equals(player.getSpectatorTarget())) {
            player.setSpectatorTarget(mob);
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
