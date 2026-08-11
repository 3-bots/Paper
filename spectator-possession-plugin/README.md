# SpectatorPossession

A Paper plugin, built against this repo's own API (Minecraft/Paper 26.2),
that upgrades vanilla's spectator "look through a mob's eyes" feature into
actually controlling it.

## What it does

Right-click a mob while in spectator mode to possess it:

- The mob's own AI is disabled and your camera locks to it (via vanilla's
  spectator-target mechanism), same as the normal vanilla feature.
- Unlike vanilla, the mob now **follows your flight exactly** - fly around
  as a spectator and the mob moves with you, so you're effectively
  steering it wherever you go.
- **Sneak** to trigger the mob's special ability, if it has one. Right now
  only Creeper is wired up: sneaking makes it explode immediately
  (damages/knocks back nearby entities; only breaks blocks if
  `settings.creeper-explosion-breaks-blocks` is true) and removes the
  creeper, releasing you back to free spectating.
- **Swap hands (F)** or run `/possess release` to let go of the mob
  voluntarily - its AI comes back and your camera returns to normal.
- Possession also ends automatically if the mob dies, you leave spectator
  mode, or you disconnect.

Only one player can possess a given mob at a time.

## Adding more mob abilities

`AbilityRegistry` maps `EntityType` to a `MobAbility` implementation - add
a new class implementing that interface (see `CreeperExplodeAbility` for
an example) and register it there.

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
`spectator-possession-plugin/build/libs/SpectatorPossession-1.0.0.jar`.
