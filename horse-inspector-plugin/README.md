# HorseInspector

A Paper plugin for Minecraft 1.20.2 that adds a craftable **Horse
Inspector** tool for viewing horse stats, previewing breeding outcomes, and
pulling saddles off horses.

## Getting the tool

Craft it (shape below) or have an op run `/horseinspector give`:

```
G . G
. S .
G . G
```
`G` = Gold Nugget, `S` = Stick.

## Using it

- **Right-click a horse** with the tool: shows its health, speed, and jump
  stats (both the raw attribute value and an approximate real-world unit -
  hearts, blocks/second, blocks of jump height).
- **Right-click a second horse** within `selection-timeout-seconds`: shows
  that horse's stats too, plus a predicted stat range for a foal bred from
  the two (only when both are the same horse type). This isn't a byte-exact
  replica of Minecraft's internal breeding RNG - see below.
- **Shift+right-click a saddled horse** with the tool: pulls the saddle off
  and into your inventory (or drops it if your inventory is full) instead
  of needing to open the horse's inventory and drag it out manually.

## About the breeding prediction

Vanilla breeds a foal's stats by averaging the two parents about 90% of the
time (with some random variance), and about 10% of the time rerolling a
stat completely at random instead. This plugin shows the averaged value
plus a configurable +/- spread (`variance-fraction` of the stat's total
possible range) to represent that - it's a useful estimate, not a precise
simulation of the exact vanilla algorithm.

## Configuration (`config.yml`)

| Option | Description |
| --- | --- |
| `settings.recipe-enabled` | Whether the crafting recipe is registered |
| `settings.selection-timeout-seconds` | How long a horse stays selected for comparison |
| `settings.variance-fraction` | Spread shown around the averaged breeding prediction |
| `ranges.*` | Vanilla min/max for health, speed, and jump strength |

## Custom icon

The tool has a custom horseshoe + magnifying glass icon instead of a plain
stick, via `resourcepack/` in this directory (a `CustomModelData` of `1001`
on the item, with a resource pack override on `item/stick` pointing at a
new model/texture). This only shows up for players if the resource pack is
actually applied - it's a separate file from the plugin jar:

1. Zip the contents of `resourcepack/` (`pack.mcmeta` and `assets/` at the
   zip root, not nested in a subfolder).
2. Upload that zip in Minehut's server settings under **Resource Pack**, or
   host it and set `resource-pack` / `resource-pack-sha1` in
   `server.properties` if not on Minehut.
3. Without the pack applied, the tool still works fine - it just looks like
   a plain stick.

## Commands

- `/horseinspector give` (permission `horseinspector.give`, default: op)
- `/horseinspector reload` (permission `horseinspector.reload`, default: op)

## Building

```
./gradlew build
```

Jar output: `build/libs/HorseInspector-1.0.0.jar`.
