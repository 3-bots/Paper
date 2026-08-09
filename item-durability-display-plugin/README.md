# ItemDurabilityDisplay

A Paper plugin, built against this repo's own API (Minecraft/Paper 26.2 -
this is a new plugin, so unlike the older plugins in this repo it targets
the server's actual version instead of legacy 1.20.2).

## What it does

Vanilla only shows durability as a colored bar under the item icon - no
exact numbers. That bar doesn't always render clearly for Bedrock players
connecting through Geyser, so this plugin adds a plain tooltip line
instead:

```
Durability: 42 / 64
```

The line is colored green/yellow/red depending on how much durability is
left, or shows "Unbreakable" for items with that flag set. It works for
every player (Java or Bedrock) since it's just an ordinary lore line -
nothing Bedrock-specific is required for it to show up on both.

A repeating task (`settings.update-interval-ticks`, default every 10
ticks / 0.5s) scans every online player's main inventory, armor, and
offhand, and adds or refreshes the line on any damageable item. Running
it as a sweep rather than hooking specific damage events means it stays
correct no matter what caused the durability to change - taking damage,
an anvil/grindstone repair, mending, creative-mode edits, etc.

## Configuration (`config.yml`)

| Option | Description |
| --- | --- |
| `settings.update-interval-ticks` | How often the tooltip refresh runs, in ticks (default 10 = 0.5s) |

## Commands

- `/itemdurabilitydisplay reload` (alias `/durability reload`, permission `itemdurabilitydisplay.reload`, default: op)

## Building

Requires JDK 25 (this plugin targets Paper 26.2, which needs it).

```
./gradlew build
```

Jar output: `build/libs/ItemDurabilityDisplay-1.0.0.jar`.
