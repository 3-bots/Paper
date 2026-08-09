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

That villager immediately gets the matching profession, claims the new
block as its job site, and plays the usual green "happy villager" particle
burst - no waiting on vanilla's usual delay for either.

Setting profession/job-site through the API doesn't register the claim
with vanilla's own internal point-of-interest reservation system, so the
villager's own AI can occasionally notice the mismatch a tick or two later
and silently clear it again. The assignment is reasserted a few times over
the following ~2 seconds to win that race instead of losing to it.

Villagers that already have a profession are never touched by this, even
if they haven't actually traded with anyone yet - only genuinely jobless
villagers get auto-claimed.

A periodic sweep (`scan-interval-ticks`, default every 5s) also catches
workstations that already existed before this plugin was running, or
jobless villagers that wander near one later - placement events alone only
cover brand new blocks. Every successful claim (from either path) logs a
line to console, so you can confirm what happened.

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
| `settings.scan-block-radius` | How far each jobless villager looks around itself during the periodic sweep (default 8) |
| `settings.scan-interval-ticks` | How often the sweep runs (default 100 = 5s) |

## Commands

- `/villagerinstantjob reload` (alias `/vjob reload`, permission `villagerinstantjob.reload`, default: op)
- `/villagerinstantjob scan` - run the sweep immediately instead of waiting for the next interval

## Building

```
./gradlew build
```

Jar output: `build/libs/VillagerInstantJob-1.1.3.jar`.
