# VillagerWorkstation

A Paper plugin for Minecraft 1.20.2 that keeps villagers close to their
claimed workstation during the day.

## What it does

Every `check-interval-ticks` (default 5 seconds), it scans all villagers in
all loaded worlds. During the day (skipped at night, so sleeping villagers
aren't yanked out of bed), any villager whose claimed workstation (lectern,
composter, smithing table, etc. - read from its `JOB_SITE` memory) is more
than `max-distance` blocks away gets teleported to a safe spot right next
to it.

"Safe spot" means an open block adjacent to the workstation with solid
ground underneath and headroom above - it won't drop a villager into a
wall or off a ledge.

## Configuration (`config.yml`)

| Option | Description |
| --- | --- |
| `settings.check-interval-ticks` | How often to scan (default 100 = 5s) |
| `settings.max-distance` | How far a villager can drift before being snapped back (default 1.5) |
| `settings.only-daytime` | Only snap during the day (default true) |

## Commands

- `/villagerworkstation reload` (alias `/vwork reload`, permission `villagerworkstation.reload`, default: op)

## Building

```
./gradlew build
```

Jar output: `build/libs/VillagerWorkstation-1.0.0.jar`.
