# SimpleHomes

A multi-home Paper plugin for Minecraft 1.20.2. Every player gets one free
home; each additional home slot costs resources taken from the player's
inventory when it's set.

## Default cost table (`config.yml` -> `costs`)

| Home # | Cost |
| --- | --- |
| 1 | Free |
| 2 | 1x Diamond Block |
| 3 | 1x Netherite Block |
| 4 | 1x Nether Star + 1x Diamond Block + 1x Netherite Block |

Edit the `costs` section in `config.yml` to change items/amounts, or
`settings.free_homes` / `settings.max_homes` to change how many are free vs.
the total cap.

## Commands

- `/sethome [name]` — set your default home (no name) or a named home. Charges
  the configured cost if this isn't a free slot.
- `/delhome [name]` — delete a home.
- `/home [name]` — teleport to a home after the configured delay; moving
  cancels it.
- `/homes` (alias `/myhomes`) — list your homes, with each one's world and
  coordinates. Use the `/myhomes` alias if `/homes` is claimed by another
  plugin on your server (Minehut bundles some built-in commands that can
  shadow a plugin's own command of the same name).
- `/simplehomes reload` (`simplehomes.reload`, default: op) — reload config.
- `/tpa <player>` — ask to teleport to another player. They get
  `/tpaccept`/`/tpdeny` to respond.
- `/tpahere <player>` — ask another player to teleport to you instead.
- `/tpaccept` (alias `/tpyes`) — accept the pending request, if any.
- `/tpdeny` (alias `/tpno`) — deny it.

Requests expire after `settings.tpa_expiry` seconds (default 120, 0 =
never). Accepted teleports go through the same delay-and-cancel-on-move
behavior as `/home`. Permission `simplehomes.tpa` (default: true) gates
all four tpa commands.

Permission `simplehomes.bypasscost` (default: op) skips the resource charge.

## Building

```
./gradlew build
```

Jar output: `build/libs/SimpleHomes-1.2.1.jar`.

Homes are stored per-player under `plugins/SimpleHomes/playerdata/<uuid>.yml`
and persist across restarts.

If a player already has a file at that path from a different plugin also
named "SimpleHomes" that stores `defaultHome`/`customHomes` as raw
`org.bukkit.Location` values, it's read automatically the first time that
player's homes are loaded and treated as their existing homes - nothing
needs to be done manually to migrate it. The file gets rewritten in this
plugin's own format the next time that player's homes are saved (e.g. on
their next `/sethome`/`/delhome`, or on server shutdown).
