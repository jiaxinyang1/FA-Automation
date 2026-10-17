**Unofficial automation addon for [Forbidden & Arcanus](https://www.curseforge.com/minecraft/mc-mods/forbidden-arcanus) (1.21.1, NeoForge). Not affiliated with the mod — it only calls its public APIs and requires the original mod to be installed.**

This addon restores the item automation that was lost when Forbidden & Arcanus moved to the NeoForge capability system (see [stal111/Forbidden-Arcanus#466](https://github.com/stal111/Forbidden-Arcanus/issues/466)), and adds fluid and ritual automation on top.

## Item automation (hoppers & pipes)

* **Hephaestus Forge** — the four essence input slots accept items that can be converted into their matching essence; the main slot accepts a single ritual ingredient; enhancer slots stay GUI-only.
* **Pedestals** — single-slot item access that keeps the forge's ritual ingredient cache in sync.
* **Clibano** — top face accepts smelting inputs, bottom face ejects results, horizontal sides accept fuel and soul items.

## Ritual safety

* While a ritual is running, pedestal insertion/extraction and main slot access are locked — changing ingredients mid-ritual would make it fail.
* Pipes can only extract a **finished ritual result** from the main slot, never an ingredient that is still waiting for the ritual to start, so insert/extract pipe setups do not yo-yo the main ingredient.

## Essence fluids

Registers four non-placeable fluids — aureal, souls, blood and experience — with **1 mB = 1 essence unit**:

* The forge exposes four tanks (capacity scales with forge tier); each essence utrem jar exposes a single tank matching its essence type.
* Store and pipe essences with any fluid automation, e.g. **Applied Energistics 2** fluid cells and export buses.

## Auto mode

Sneak-right-click the Hephaestus Forge with a **Blacksmith Gavel** (any tier) to toggle auto mode (sound + action bar feedback, saved per world). While enabled, the forge automatically starts a ritual whenever one is valid: structure complete, ingredients on pedestals and in the main slot, and enough essences stored.

Rituals are attributed to the player who enabled auto mode; while that player is offline, auto-starting pauses.

## Requirements

* Minecraft 1.21.1, NeoForge
* [Forbidden & Arcanus](https://www.curseforge.com/minecraft/mc-mods/forbidden-arcanus) 2.6.1+
* Valhelsia Core

## Source & issues

[github.com/jiaxinyang1/FA-Automation](https://github.com/jiaxinyang1/FA-Automation) — MIT licensed.
