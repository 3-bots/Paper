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
- A book pops open immediately, listing every ability that mob has and
  which number key (1-4) triggers each one. Right now only Creeper is
  wired up: key **1** detonates it immediately (damages/knocks back
  nearby entities; only breaks blocks if
  `settings.creeper-explosion-breaks-blocks` is true), which also
  releases you back to free spectating since the creeper is gone.
- **Swap hands (F)** or run `/possess release` to let go of the mob
  voluntarily - its AI comes back and your camera returns to normal.
- Possession also ends automatically if the mob dies, you leave spectator
  mode, or you disconnect.

Only one player can possess a given mob at a time.

## Adding more mob abilities

`AbilityRegistry` maps `EntityType` to a list of `AbilityDefinition`s, each
bound to a hotbar key (1-4) with a name and description shown in the
popup book. Add a new `MobAbility` implementation (see
`CreeperExplodeAbility` for an example) and register it with a free slot
number for that mob type.

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
`spectator-possession-plugin/build/libs/SpectatorPossession-1.1.0.jar`.
