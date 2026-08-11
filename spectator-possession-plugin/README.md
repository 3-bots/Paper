# SpectatorPossession

A Paper plugin, built against this repo's own API (Minecraft/Paper 26.2),
that upgrades vanilla's spectator "look through a mob's eyes" feature into
actually controlling it.

## What it does

Left-click (attack) a mob while in spectator mode to possess it. Vanilla
already binds left-click on any entity, while spectating, to its own
built-in "look through its eyes" camera-lock (`Player#setSpectatorTarget`) -
that's a real client/server feature, not something this plugin has to
invent a trigger for. This intercepts it via Paper's
`PlayerStartSpectatingEntityEvent` (cancellable, fires right before vanilla's
camera-lock would kick in) instead of the passive vanilla behavior:

- The mob's own AI is disabled and you're teleported right into its eye
  spot. Your camera then genuinely locks onto the mob via vanilla's own
  `Player#setSpectatorTarget` - you see exactly what it sees. Vanilla
  normally auto-releases that lock the instant you move (that's the real
  mechanism behind the classic "spectating is view-only" behavior - not
  that WASD gets disabled, just that moving detaches the camera), so this
  re-applies the lock every single time it slips, every move tick,
  keeping the through-its-eyes view effectively permanent while your
  movement input itself was never actually blocked.
- Your movement drives the mob with real **velocity**, not a raw
  teleport - so it moves like it actually would: a chicken stays grounded
  (gravity pulls it back down even if you try to fly it upward), a
  naturally airborne mob stays aloft, and it's all smooth physics instead
  of snapping to your position every tick (which also caused visible
  camera hitches, since the camera is locked to an entity that kept
  getting teleported). The mob also keeps turning to face wherever you
  look, and flying through water looks and feels right, since the
  underwater tint is tied to camera position, not gamemode.
- **Left-click (swing your arm) to attack with the mob** - spectators can
  never deal damage themselves through any normal path, so this hooks
  arm-swing directly (fires on every left-click no matter what, unlike
  the spectate-lock event above, which likely only fires once) and
  raytraces from the mob's own eyes for whatever's in range, dealing
  damage from the mob using its own `Attribute.ATTACK_DAMAGE` (a flat 1.0
  for mobs without one, e.g. possessing a passive animal).
- You immediately get a chat message confirming possession (includes the
  plugin's version number - a quick way to check you're actually running
  the jar you think you are). A book pops open a tick later (deferred
  slightly so the book-open and scoreboard packets don't land the same
  tick as the teleport above - the client silently drops them if they
  do), and a **sidebar** (top-right of your screen) stays up the whole
  time you're possessing: the mob's name, its current/max health (a
  heart line, refreshed every half-second - spectator mode never shows
  a normal hearts HUD, so this is the only way to see it), and every
  ability it has with which hotbar key (1-9, not just 1-4 - however many
  a mob has) triggers each one. Right now only Creeper is wired up:
  key **1** detonates it immediately (damages/knocks back nearby
  entities; only breaks blocks if `settings.creeper-explosion-breaks-blocks`
  is true), which also releases you back to free spectating since the
  creeper is gone.
- **Swap hands (F)** or run `/possess release` to let go of the mob
  voluntarily - its AI comes back and the sidebar disappears. You stay
  wherever you were (right where the mob was), free to fly off as a
  normal spectator again. Both are a guaranteed way out regardless of
  internal state - even if something left you stuck watching through a
  mob with nothing on record as possessed, release still clears it
  instead of claiming "you aren't possessing anything."
- Possession also ends automatically (with the same cleanup) if the mob
  dies, you leave spectator mode, or you disconnect.

Only one player can possess a given mob at a time.

## Adding more mob abilities

`AbilityRegistry` maps `EntityType` to a list of `AbilityDefinition`s, each
bound to a hotbar key (1-9, one per key - registering a second ability on
an already-used slot for the same mob throws) with a name and description
shown in both the popup book and the sidebar. Add a new `MobAbility`
implementation (see `CreeperExplodeAbility` for an example, including how
it explicitly calls `PossessionManager#stopPossessing` since
`Entity#remove()` doesn't fire `EntityDeathEvent`) and register it with a
free slot number for that mob type.

## Configuration (`config.yml`)

| Option | Description |
| --- | --- |
| `settings.creeper-explosion-breaks-blocks` | Whether a possessed creeper's explosion breaks blocks (default false) |

## Commands

- `/possess release` - let go of whatever mob you're possessing
- `/possess reload` (permission `spectatorpossession.reload`, default: op) - reload config

## Building

Same as the other plugins here that depend on the repo's own `paper-api`
module. Build from the **repo root**:

```
./gradlew :spectator-possession-plugin:build
```

Requires JDK 25. Jar output:
`spectator-possession-plugin/build/libs/SpectatorPossession-1.5.0.jar`.
