# SimpleHomes

A multi-home Paper plugin for Minecraft 1.20.2. Every home slot (up to
`settings.max_homes`, 4 by default) is free by default - `settings.free_homes`
is set equal to `settings.max_homes` out of the box, and `costs` is empty.

## Charging for homes (optional)

To charge resources for homes past a certain slot, lower `settings.free_homes`
below `settings.max_homes` and add entries back to the `costs` section in
`config.yml`, keyed by home slot number. Example:

```yaml
settings:
  free_homes: 1

costs:
  2:
    - material: DIAMOND_BLOCK
      amount: 1
  3:
    - material: NETHERITE_BLOCK
      amount: 1
```

Each entry is a list of items (all required together) taken from the
player's inventory when that home slot is set. Slots beyond the highest
numbered entry in `costs` reuse the last defined cost.

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
  `/tpaccept`/`/tpdeny` to respond. `<player>` only needs to be enough of
  their name to match uniquely, not the full thing.
- `/tpahere <player>` — ask another player to teleport to you instead.
  Same partial-name matching as `/tpa`.
- `/tpaccept` (alias `/tpyes`) — accept the pending request, if any.
- `/tpdeny` (alias `/tpno`) — deny it.

Requests expire after `settings.tpa_expiry` seconds (default 120, 0 =
never). Accepted teleports go through the same delay-and-cancel-on-move
behavior as `/home`. Permission `simplehomes.tpa` (default: true) gates
all four tpa commands.

`/tpa`/`/tpahere` work normally for and to players in spectator mode too.

Vanilla Minecraft's own spectator-menu teleport (clicking a player's name
in the tab/player list while in spectator mode, which jumps you straight
to their location) is blocked outright, separately from TPA - that's a
built-in client feature, not a command.

Permission `simplehomes.bypasscost` (default: op) skips the resource charge.

## Building

```
./gradlew build
```

Jar output: `build/libs/SimpleHomes-1.2.8.jar`.

Homes are stored per-player under `plugins/SimpleHomes/playerdata/<uuid>.yml`
and persist across restarts.

If a player already has a file at that path from a different plugin also
named "SimpleHomes" that stores `defaultHome`/`customHomes` as raw
`org.bukkit.Location` values, it's read automatically the first time that
player's homes are loaded and treated as their existing homes - nothing
needs to be done manually to migrate it. The file gets rewritten in this
plugin's own format the next time that player's homes are saved (e.g. on
their next `/sethome`/`/delhome`, or on server shutdown).

If that automatic migration doesn't pick anything up for a given player
(their file yields nothing in either format), the server log will say so
with a warning naming the player's UUID - check that against the actual
worlds loaded on the server. `LegacyHomeSeed.java` also carries hardcoded
recovery data for specific known players as an unconditional last resort
if the log warning shows up for them again.
