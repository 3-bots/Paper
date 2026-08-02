# DaysDisplay

A small Paper plugin that shows the current in-game day counter on players' HUD.
Built for Minecraft 1.20.2 (compatible with Minehut Paper 1.20.2 servers). Since
it only sends normal chat/UI elements through the vanilla protocol, it works for
Bedrock players connecting through Geyser with no extra setup.

## Why not literally under the coordinates?

The coordinate overlay (`Position: x, y, z`) is rendered entirely client-side —
plugins running on the server can't draw into it or place something directly
below it. `ACTIONBAR` mode (the default) is the closest always-visible,
non-intrusive equivalent: it's a persistent line of text near the top of the
player's view, refreshed continuously, without needing a resource pack. Two
other modes are available if you'd rather have it as a boss bar or a
scoreboard sidebar entry — see `display-mode` below.

## Building

```
./gradlew build
```

The compiled jar will be at `build/libs/DaysDisplay-1.0.0.jar`.

## Installing on Minehut

1. Build the jar (or download it from wherever you built it).
2. In the Minehut panel, go to your server's **Plugins** tab and upload
   `DaysDisplay-1.0.0.jar` (custom plugin uploads require Minehut's paid tier).
3. Restart the server.
4. A `config.yml` will be generated in `plugins/DaysDisplay/` on first run.

## Configuration (`plugins/DaysDisplay/config.yml`)

| Option | Description |
| --- | --- |
| `display-mode` | `ACTIONBAR`, `BOSSBAR`, or `SCOREBOARD` |
| `update-interval-ticks` | How often the display refreshes (20 ticks = 1 second) |
| `format` | Display text, `%day%` is replaced with the day number, `&` = color codes |
| `day-offset` | Added to the computed day so day 0 shows as "Day 1" by default |
| `boss-bar-color` | Used only in `BOSSBAR` mode |
| `scoreboard-title` | Used only in `SCOREBOARD` mode |

## Commands

- `/daysdisplay reload` (`daysdisplay.reload`, default: op) — reload the config
- `/daysdisplay toggle` (`daysdisplay.toggle`, default: everyone) — toggle the
  display on/off for yourself

Day count is computed per-player from `world.getFullTime() / 24000`, i.e. the
survival day count of whichever world the player is currently in.
