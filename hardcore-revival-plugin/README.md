# HardcoreRevival

A Paper plugin, built against this repo's own API (Minecraft/Paper 26.2),
that lets players ritually bring a hardcore-dead player back from
spectator mode.

## What it does

When a player dies in a hardcore world, they're marked as awaiting
revival (this uses vanilla's own hardcore behavior of dropping them into
spectator mode - the plugin doesn't touch that). They need to stay online
in spectator for the ritual below to work on them.

**Build the altar.** Around a Fire block (configurable), somewhere within
`altar-radius`, place at least `iron-blocks-required` Iron Blocks,
`diamond-blocks-required` Diamond Blocks, one Torch placed directly on top
of a Gold Block ("gold torch"), and one Redstone Torch. Exact arrangement
doesn't matter, just that everything is present within range of the fire.

**Write the name.** Write the dead player's name somewhere in a book
(Book and Quill or a signed book both work). Once you're holding it near
a complete altar and the name matches someone currently awaiting revival,
the book's display name turns red as confirmation, and the sky itself
tints red for you too - it clears back to normal the moment you and the
book move away from the altar, or the name stops matching (e.g. once the
ritual finishes). Matching ignores dots on either side of the name, so a
Bedrock player whose Java name is Floodgate-prefixed (like
`.Mrkumi1212`) matches whether or not you type the dot.

**Sacrifice a villager.** Kill a villager within `altar-radius` of a
complete altar. Particles stream from where it died into the fire, and
that altar is "armed" for `sacrifice-window-seconds` (default 60s).

**Burn the book.** While the altar is armed, right-click the fire holding
the red book. If everything still checks out - altar valid, sacrifice
still within its window, name matches, and the target is online in
spectator - the book is consumed, lightning strikes the altar (visual
only, no damage), and the named player is set back to survival with full
health at the altar.

**The ritual has a cost.** The moment the player is revived, every
Iron/Gold/Diamond Block within `altar-radius` of the fire independently
rolls `ritual-block-loss-chance` - blocks that hit get struck by real
lightning and disappear. It's random how many (if any) you lose each
time, so a bigger altar than the minimum required is a buffer, not just
overkill.

## Configuration (`config.yml`)

| Option | Description |
| --- | --- |
| `settings.fire-material` | Block the ritual is centered on / the book is thrown into (default `FIRE`) |
| `settings.altar-radius` | Search radius in blocks for altar components and sacrifice proximity (default 3) |
| `settings.iron-blocks-required` | Minimum Iron Blocks needed near the fire (default 4) |
| `settings.diamond-blocks-required` | Minimum Diamond Blocks needed near the fire (default 4) |
| `settings.sacrifice-window-seconds` | How long a sacrifice keeps an altar armed (default 60) |
| `settings.ritual-block-loss-chance` | Per-block chance (0.0-1.0) for each Iron/Gold/Diamond Block near the fire to be struck and destroyed when the revival completes (default 0.3) |

## Commands

- `/hardcorerevival reload` (alias `/revival reload`, permission `hardcorerevival.reload`, default: op)

## Building

Same as `item-durability-display-plugin`/`stealthbeacon-plugin` - depends
on this repo's own `paper-api` module. Build from the **repo root**:

```
./gradlew :hardcore-revival-plugin:build
```

Requires JDK 25. Jar output:
`hardcore-revival-plugin/build/libs/HardcoreRevival-1.2.0.jar`.

Players awaiting revival are stored in
`plugins/HardcoreRevival/pending.yml` and persist across restarts.
