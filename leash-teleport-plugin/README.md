# LeashTeleport

A Paper plugin for Minecraft 1.20.2 that:

- Lets you leash villagers, which vanilla doesn't normally allow (holding a
  lead and right-clicking a villager leashes it instead of opening trades).
- Rescues entities you have leashed (including ones riding in a boat or
  other vehicle) when a teleport — from `/home` in another plugin, a warp,
  `/tp`, etc. — snaps the leash.
- Brings along whatever you're riding (a horse, boat, etc.) when you
  teleport, instead of vanilla's default of dismounting you and leaving it
  behind.

## How it works

Trying to move leashed entities in the exact instant of a teleport turned
out to be unreliable across different plugins and long-distance teleports.
Instead, this plugin:

1. Tracks a short position history (last ~12 seconds) for every online
   player.
2. Listens for `EntityUnleashEvent` with reason `DISTANCE` (the leash
   actually snapping because the holder got too far away).
3. When that happens, compares the holder's current position to their
   position from `detect-window-seconds` ago. If they moved more than
   `teleport-detect-blocks`, or changed world, that's treated as a
   teleport rather than the pet simply falling behind.
4. The entity (and its vehicle + any other passengers, if it was riding
   one) is teleported to the player's current location and re-leashed.

A normal leash break (the pet got stuck, fell behind on foot, etc.) is left
alone — the lead drops as vanilla intends.

## Configuration (`config.yml`)

| Option | Description |
| --- | --- |
| `settings.teleport-detect-blocks` | Distance jump (blocks) that counts as a teleport (default 20) |
| `settings.detect-window-seconds` | How far back to compare the player's position (default 5) |
| `settings.spread-radius` | Spacing between multiple rescued entities at the player's location |
| `settings.notify` | Whether to message the player when entities are brought back |

## Commands

- `/leashteleport reload` (alias `/ltp reload`, permission `leashteleport.reload`, default: op)

## Building

```
./gradlew build
```

Jar output: `build/libs/LeashTeleport-1.0.0.jar`.
