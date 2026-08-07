# SugarCaneBonemeal

A Paper plugin for Minecraft 1.20.2. Vanilla dispensers can fertilize crops
like wheat or bamboo with bone meal, but sugar cane isn't supported at all -
the bone meal just pops out as a dropped item. This plugin adds that:

A dispenser loaded with bone meal, facing a sugar cane block, will grow that
cane by one block (up to `settings.max-height`, default 3 - vanilla's
natural max) each time it's triggered, consuming one bone meal per growth
and playing the usual bone meal particle + sound.

## Configuration (`config.yml`)

| Option | Description |
| --- | --- |
| `settings.max-height` | Tallest a cane column can be grown to (default 3) |
| `settings.play-effects` | Whether to play the bone meal particle/sound on growth |

## Commands

- `/sugarcanebonemeal reload` (alias `/scbm reload`, permission `sugarcanebonemeal.reload`, default: op)

## Building

```
./gradlew build
```

Jar output: `build/libs/SugarCaneBonemeal-1.0.0.jar`.
