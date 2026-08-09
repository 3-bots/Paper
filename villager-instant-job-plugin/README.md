# VillagerInstantJob

A Paper plugin for Minecraft 1.20.2. Normally, an unemployed villager
takes a little while to notice a newly placed workstation and claim it.
This makes that instant.

## What it does

When you place a workstation block (lectern, composter, smithing table,
etc.), the plugin looks within `search-radius` blocks for the nearest
villager that:

- is an adult,
- has no profession (`Profession.NONE`), and
- has no job site already claimed.

That villager immediately gets the matching profession and claims the new
block as its job site - no waiting on vanilla's usual delay.

Villagers that already have a profession are never touched by this, even
if they haven't actually traded with anyone yet - only genuinely jobless
villagers get auto-claimed.

## Profession mapping

| Block | Profession |
| --- | --- |
| Blast Furnace | Armorer |
| Smoker | Butcher |
| Cartography Table | Cartographer |
| Brewing Stand | Cleric |
| Composter | Farmer |
| Barrel | Fisherman |
| Fletching Table | Fletcher |
| Cauldron | Leatherworker |
| Lectern | Librarian |
| Stonecutter | Mason |
| Loom | Shepherd |
| Smithing Table | Toolsmith |
| Grindstone | Weaponsmith |

## Configuration (`config.yml`)

| Option | Description |
| --- | --- |
| `settings.search-radius` | How far to look for a jobless villager around a newly placed workstation (default 16) |

## Commands

- `/villagerinstantjob reload` (alias `/vjob reload`, permission `villagerinstantjob.reload`, default: op)

## Building

```
./gradlew build
```

Jar output: `build/libs/VillagerInstantJob-1.0.0.jar`.
