# SpectatorPossession

A Paper plugin, built against this repo's own API (Minecraft/Paper 26.2),
that lets a spectator (e.g. a hardcore-dead player awaiting revival) take
real control of a mob.

## What it does

Left-click (attack) a mob while in spectator mode to possess it. Vanilla
already binds left-click on any entity, while spectating, to its own
built-in "look through its eyes" camera-lock. This intercepts that via
Paper's `PlayerStartSpectatingEntityEvent` (cancellable, fires right before
vanilla's camera-lock would kick in) and takes over instead:

- **You stop being a spectator and become an ordinary, invisible
  Creative-mode player.** Earlier versions tried to keep you a spectator
  and force vanilla's own camera-lock (`Player#setSpectatorTarget`) to
  stay attached - in practice that turned out unreliable (the lock
  wouldn't stick, movement wouldn't apply to the mob, combat didn't
  work). This is the same trick real disguise/morph plugins use: rather
  than hack a spectator's camera onto a separate entity, you're just
  *you* again, with completely native flight, hearts, hunger, movement,
  and combat - none of it faked or fought against vanilla. The mob is a
  "costume" that's kept mirrored to wherever you actually are, so it
  looks and acts like you're inside it, but every mechanic underneath is
  the real, ordinary player mechanic. It comes with one honest trade-off:
  since you're a normal-height, normal-eye-position player wearing a
  chicken as a costume rather than literally seeing through a chicken's
  eyes, the camera isn't at the mob's exact eye height/model - full pixel
  perfect POV would require a client-side mod, which a server plugin
  can't install for you.
- **Your own combat just works** (you're not a spectator anymore, so
  nothing is blocking it) - damage dealt is corrected to match the mob's
  own `Attribute.ATTACK_DAMAGE` (a flat 1.0 for mobs without one, e.g.
  possessing a passive animal) instead of whatever your held item would
  normally do.
- You immediately get a chat message confirming possession (includes the
  plugin's version number - a quick way to check you're actually running
  the jar you think you are). A book pops open a tick later, and a
  **sidebar** (top-right of your screen) stays up the whole time you're
  possessing: the mob's name, its current/max health (refreshed every
  half-second), and every ability it has with which hotbar key (1-9, not
  just 1-4 - however many a mob has) triggers each one. Right now only
  Creeper is wired up: key **1** detonates it immediately (damages/knocks
  back nearby entities; only breaks blocks if
  `settings.creeper-explosion-breaks-blocks` is true), which also
  releases you back to spectating since the creeper is gone.
- **Swap hands (F)** or run `/possess release` to let go of the mob
  voluntarily - you're switched back to your original gamemode (spectator,
  in the normal hardcore-death case) and made visible again. Both are a
  guaranteed way out regardless of internal state - even if something
  left you stuck, release still clears it instead of claiming "you aren't
  possessing anything."
- Possession also ends automatically (with the same cleanup) if the mob
  dies, your gamemode changes to anything other than the Creative this
  plugin put you in (e.g. an op runs `/gamemode`), or you disconnect.

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
`spectator-possession-plugin/build/libs/SpectatorPossession-2.0.0.jar`.
