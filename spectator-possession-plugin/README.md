# SpectatorPossession

A Paper plugin, built against this repo's own API (Minecraft/Paper 26.2),
that upgrades vanilla's spectator "look through a mob's eyes" feature into
actually controlling it.

## What it does

Right-click a mob while in spectator mode to possess it:

- The mob's own AI is disabled and you're teleported right into its eye
  spot as a normal free-flying spectator. This deliberately does **not**
  use vanilla's `Player#setSpectatorTarget` camera-lock ("view through an
  entity") - that hijacks input client-side so your own WASD stops moving
  anything at all, which is real vanilla behavior, not a bug in the
  possession-target mob, and it's why an earlier build of this plugin left
  you stuck passively watching through the mob's eyes with no control.
  Staying a normal spectator means your movement keeps working exactly as
  it always does.
- The mob **follows your flight exactly** - fly around as a spectator and
  the mob moves with you, so you're effectively steering it wherever you
  go. Since this is direct position-following with none of the mob's
  normal movement constraints, this already covers every movement style
  uniformly - a possessed spider can go up walls and across ceilings, a
  blaze can hover in midair, an ender dragon can fly, all the same way,
  because nothing is stopping any of them from going anywhere you fly.
- A book pops open immediately, and a **sidebar** (top-right of your
  screen) stays up the whole time you're possessing, both listing every
  ability that mob has and which hotbar key (1-9, not just 1-4 - however
  many a mob has) triggers each one. Right now only Creeper is wired up:
  key **1** detonates it immediately (damages/knocks back nearby
  entities; only breaks blocks if `settings.creeper-explosion-breaks-blocks`
  is true), which also releases you back to free spectating since the
  creeper is gone.
- **Swap hands (F)** or run `/possess release` to let go of the mob
  voluntarily - its AI comes back and the sidebar disappears. You stay
  wherever you were (right where the mob was), free to fly off as a
  normal spectator again.
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
`spectator-possession-plugin/build/libs/SpectatorPossession-1.2.1.jar`.
