# StealthBeacon

A Paper plugin, built against this repo's own API (Minecraft/Paper 26.2),
that hides nearby players' nametags without any vanilla beacon beam.

## What it does

Place a marker block (a Sea Lantern by default, configurable) on top of a
diamond block pyramid, laid out exactly like a vanilla beacon pyramid - a
3x3 ring of diamond blocks directly beneath it, a 5x5 ring below that, then
7x7, then 9x9 for the maximum tier 4. No real Minecraft Beacon block is
involved anywhere, so there's never a beam.

The number of complete rings underneath a marker (its "tier", 1-4) is
re-checked periodically and determines the size of an invisible zone
centered on that block: a horizontal radius and a separate vertical
range (how far up/down it reaches), both configurable per tier. Any
online player standing inside that zone - in any active beacon's zone,
anywhere on the server - has their nametag hidden from everyone else for
as long as they stay in range. Leaving every zone restores it.

Right-click a marker block to see its current tier and the exact radius
it's covering.

Nametag hiding is done with a scoreboard team (`NAME_TAG_VISIBILITY:
NEVER`). Since Bukkit only allows a player on one scoreboard team at a
time, being inside a beacon's zone will remove a player from any other
team they're on (e.g. a rank-color team) for as long as they're hidden.

## Configuration (`config.yml`)

| Option | Description |
| --- | --- |
| `settings.marker-material` | Block type that acts as the beacon marker (default `SEA_LANTERN`) |
| `settings.update-interval-ticks` | How often pyramids/zones are re-checked (default 20 = 1s) |
| `settings.tier-horizontal-radius` | Horizontal radius in blocks per tier, `[tier1, tier2, tier3, tier4]` |
| `settings.tier-vertical-radius` | Vertical range (up/down) in blocks per tier |

## Commands

- `/stealthbeacon reload` (alias `/sbeacon reload`, permission `stealthbeacon.reload`, default: op)

## Building

Same as `item-durability-display-plugin` - not a standalone Gradle
project, it depends on this repo's own `paper-api` module. Build it from
the **repo root**:

```
./gradlew :stealthbeacon-plugin:build
```

Requires JDK 25. Jar output:
`stealthbeacon-plugin/build/libs/StealthBeacon-1.0.1.jar`.

Registered beacon marker locations are stored in
`plugins/StealthBeacon/beacons.yml` and persist across restarts.
