# LeashTeleport

A Paper plugin for Minecraft 1.20.2 that brings entities you have leashed
along whenever you teleport — including entities riding in a boat (or any
other vehicle) at the time. Works with any teleport, so it applies to
`/home` from a separate homes plugin, warps, `/tp`, etc. — no integration
needed with whatever plugin actually performs the teleport.

## How it works

On every `PlayerTeleportEvent`, it looks within `search-radius` blocks of
where you teleported *from* for any living entity leashed to you. For each
one found:

- If it's riding a vehicle (e.g. a boat), the vehicle and all its passengers
  are moved to your destination together, then re-mounted.
- Otherwise the entity is teleported directly.
- The leash is then re-attached to you at the destination.

Multiple brought-along entities are spread out slightly (`spread-radius`) so
they don't stack on the same block.

## Configuration (`config.yml`)

| Option | Description |
| --- | --- |
| `settings.search-radius` | How far to look for leashed entities before teleporting (default 15 blocks) |
| `settings.spread-radius` | Spacing between multiple brought-along entities at the destination |
| `settings.notify` | Whether to message the player how many entities were brought along |

## Commands

- `/leashteleport reload` (alias `/ltp reload`, permission `leashteleport.reload`, default: op)

## Building

```
./gradlew build
```

Jar output: `build/libs/LeashTeleport-1.0.0.jar`.
