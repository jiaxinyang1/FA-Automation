# Forbidden Arcanus Automation

[中文说明](README.zh-CN.md)

An unofficial NeoForge addon for **Forbidden & Arcanus** (1.21.1) that adds item and fluid
automation support plus an optional auto-ritual mode — without modifying the original mod.
Everything is implemented against the mod's public APIs.

This project is not affiliated with Forbidden & Arcanus. Forbidden & Arcanus is created by
stal111 and is All Rights Reserved; this addon merely calls its public APIs at runtime and
requires the original mod to be installed separately.

## Features

### Item automation (hoppers & item pipes)
Restores the automation support that was lost when the mod was ported to the NeoForge
capability system (see [stal111/Forbidden-Arcanus#466](https://github.com/stal111/Forbidden-Arcanus/issues/466)):

- **Hephaestus Forge** — the four essence input slots accept only items that can be converted
  into their matching essence; the main slot accepts a single ritual ingredient; enhancer
  slots remain GUI-only.
- **Pedestals** — single-slot item access. Pipe insertions keep the forge's ritual ingredient
  cache in sync.
- **Clibano** — reuses the mod's own `ClibanoItemHandler`: top face accepts smelting inputs,
  bottom face ejects results, horizontal sides accept fuel and soul items.

### Ritual safety
- While a ritual is running, pedestal insertion/extraction and main slot access are locked —
  changing ingredients mid-ritual would make it fail.
- The main slot only allows pipes to extract a finished ritual result, never an ingredient
  that is still waiting for the ritual to start (implemented via a result snapshot tracker,
  so insert/extract pipe setups do not yo-yo the main ingredient).

### Essence fluids
Registers four non-placeable fluids — `fa_automation:aureal / souls / blood / experience` —
with **1 mB = 1 essence unit**:

- The forge exposes four tanks (capacity scales with forge tier); each essence utrem jar
  exposes a single tank matching its essence type.
- Works with any fluid-based automation (e.g. Applied Energistics 2 fluid storage/export).
- Aureal/blood/experience textures reuse the liquid textures of the original mod;
  the souls textures are shipped with this addon (the mod has none).

### Essence jar auto-absorption
Typed jars generate their essence on their own, like the forge does for blood:
- **Blood** jars absorb `max health x 10` essence whenever a living entity dies within 4 blocks.
- **Experience** jars attract nearby experience orbs and absorb `value x 10` essence.
- **Souls** jars slowly extract 10 essence from soul sand / soul soil in a 5x5x5 area, turning it into soulless sand.

### Essence jar type switching
Sneak-right-click an **empty** essence utrem jar with an **empty hand** to cycle its essence type
(aureal -> souls -> blood -> experience, with sound + action bar feedback). Jars that still contain
essence cannot be switched.

### Auto mode
Sneak-right-click the Hephaestus Forge with a Blacksmith Gavel (any tier) to toggle auto mode
(sound + action bar feedback, persisted per world). While enabled, the forge automatically
starts a ritual whenever one is valid (structure complete, ingredients present, enough
essences). Rituals are attributed to the player who enabled auto mode; while that player is
offline, auto-starting waits.

## Building

Requires JDK 21.

1. Put two jars into `libs/` (not redistributed here for licensing reasons, see `libs/README.md`):
   - `forbidden_arcanus-2.6.1.jar` — the unmodified Forbidden & Arcanus release jar
   - `valhelsia_core-neoforge-1.21.1-1.1.5.jar` — Valhelsia Core (the mod's dependency)
2. Run:

```
./gradlew build
```

The addon jar is written to `build/libs/fa_automation-<version>.jar`.

## Installation

Put into your `mods` folder:

1. Forbidden & Arcanus 2.6.1+ (official release)
2. Valhelsia Core (its dependency)
3. `fa_automation-<version>.jar`

## Known limitations

- Auto-started rituals require the enabling player to be online (the mod's `startRitual`
  requires a player instance).
- After a server restart, a result that already sits in the main slot only becomes
  pipe-extractable again after the next completed ritual (snapshots are not persisted).
- If a multi-count ritual result is partially extracted by a pipe, the remainder can only be
  taken out via the GUI.

## License

This repository's code is licensed under the [MIT License](LICENSE).
It does not include or redistribute any assets or binaries of Forbidden & Arcanus.
