# Changelog

## 0.20.0

### Added
* **Bridge hatches.** A new family of linked hatches that send named packets between two multiblocks. Each bridge has an input and an output hatch for every tier from LV to UV, registered as `gtextras:<bridge>_input_hatch_<tier>` and `gtextras:<bridge>_output_hatch_<tier>`. The first bridge, `einstein_rosen` (Einstein-Rosen Bridge), is built in. The existing Mana, Source, Blood and Will hatches are untouched.
* **Bridge definitions in JSON.** Bridges are read at startup from `kubejs/config/gtextras_bridges.json` (so they travel with a pack's KubeJS folder) or from `config/gtextras-bridges.json`. A default file is written on first start. Each entry sets `displayName`, `color`, `returnColor`, `resonanceCap`, `resonanceDecayPerSecond` and `range`. Any number of bridges can be defined, and each gets its own hatches and multiblock abilities.
* **Packets.** A packet is a kind (`item`, `fluid`, `tag` or `data`), a name and an amount, written as one string `'<bridge> <kind> <name> <amount>'`. A `tag` packet used as a recipe input accepts any `item` packet whose item is in that tag. A `data` packet is a named number with no item behind it.
* **New recipe capability `bridge`.** KubeJS recipes can use `.input(GTX.BRIDGE, '...')` and `.output(GTX.BRIDGE, '...')` on any recipe type. Packets scale with parallel hatches and with output modifiers, like other contents. Recipe pages show them as "Bridge in" and "Bridge out" lines.
* **Linking.** The existing Link Tool links a bridge output hatch to bridge input hatches of the same bridge, up to 10 per output, within the bridge's `range`, with In order or Round robin distribution. Output hatches push packets to their linked input hatches every half second. Beams of dust particles run along a link while packets move. The colour is `color` or `returnColor` from the bridge definition, chosen per output hatch with the **Beam colour** button.
* **Recipe search.** Bridge packets take part in GregTech's recipe search. A recipe whose only input is a packet, with no item or fluid input, starts as soon as the packet has arrived in an input hatch. Recipes are matched to a hatch by bridge name first, then the recipe checks the exact packets.
* **Resonance.** `data resonance` is held by an input hatch as a level with a capacity of `resonanceCap` times the hatch tier (LV 1x up to UV 8x) and drains at `resonanceDecayPerSecond`. It does nothing by itself. Two new recipe modifiers, `GTX.bridgeYield(bridge, data, maxBonus)` and `GTX.bridgeSpeed(bridge, data, maxBonus)`, read the highest level among a machine's input hatches and scale recipe outputs or shorten recipe duration, up to `maxBonus` at a full bar.
* **Hatch screen.** Shows the stored packets in a scrolling list, the resonance bar (input hatches), the list of links with a remove button each and a Clear all links button (output hatches), the Beam colour toggle, and an Empty buffer button that needs a second click.
* **KubeJS API.** `GTX.BRIDGE` (the capability), `GTX.bridgeInput(bridge)` and `GTX.bridgeOutput(bridge)` (multiblock abilities for patterns), `GTX.bridgeYield(...)` and `GTX.bridgeSpeed(...)` (recipe modifiers).
* **Assets.** Blockstates, models, item models and a crafting recipe for each of the 16 `einstein_rosen` hatches, and overlay textures for the input and output hatch (a ring with arrows pointing in or out, with an emissive layer) in the bridge's default ice blue. Hatch recipes use the matching tier hull, circuits and plates around an Eye of Ender and are meant to be replaced by pack recipes.
* **Documentation.** The README covers bridges, packets, resonance, the definition file, the new KubeJS methods and abilities and how to add assets for a new bridge.

### Changed
* **Recipe cards show bridge packets.** In JEI the packet names are listed under the card's text as `bridge in:` and `bridge out:` (a blank line between, `data` packets as `data <name>`). The card grows to fit them and they no longer overlap the tier label or leave the card.
* The README and version are updated for 0.20.0.

### Notes
* Bridge hatches have no connection to the Mana, Source, Blood and Will hatches, their fluids config or their per hatch configuration.
* A bridge's hatch models and overlay textures are files, not settings. A bridge other than `einstein_rosen` needs its own model, blockstate and overlay files. Its beams and names follow the definition file, but the overlay colour in the texture does not.
* Packets are only counted. Items and fluids are not stored inside the hatch, so the white end of a link recreates whatever its recipe says.
* Removing a bridge from the definition file removes its hatches on the next start. Placed hatches of a removed bridge disappear.

## 0.19.7

### Fixed
* The recipe id copy (shift + left click, or the copy key) no longer writes to the log on every click and key press.
* Measuring a recipe type's card height no longer overwrites the text lines of the card being drawn at that moment.

### Added
* The log now says which version's card sizing is running and prints one line per recipe type, `[gtextras] card extra: <type> = <height>`, with the extra card height measured for that type. A type with nothing to show reads 0. If a card is stretched and its type reads 0, or the line is missing, the stretch is not coming from this build.

## 0.19.6

### Fixed
* On recipe types where GregTech already prints the "Temp" and "Coil" lines itself (for example Electric Blast Furnace style types), the coil icon now sits on the same row as GregTech's "Coil:" line instead of at the bottom of the card, and the mod no longer prints a second copy of those two lines. These types also get no extra card height.
* The extra card height is now added every time a viewer asks for the card size, instead of being baked into GregTech's cached size. A size first requested before the world had loaded can no longer leave every card stretched for the rest of the session. Until the recipes are available a type gets no extra height.

## 0.19.4

### Fixed
* The Rotation Interface now has a shaft on all six faces, so it connects to a Create gearbox or shaft whichever way it faces. It only has to touch one of the gearbox's four shaft sides.
* A multiblock controller's status panel no longer says "Running perfectly." while a recipe is waiting. It now shows "Paused: <reason>", for example the rotation message when the network is too slow. This applies to every GregTech multiblock that waits, not only rotation recipes.

## 0.19.3

### Added
* Blood hatches can be linked to Blood Altars with the Link Tool, up to 10 blocks away. Input hatches pull blood from linked altars, output hatches push blood into them. The hatch screen shows the list of linked altars with In order and Round robin distribution.

### Fixed
* Mana, Source, Blood and Will hatches now report their stored amount as fluid contents, so GregTech's recipe search can pick recipes that take these fluids. Before, a multiblock with only one of these hatches and no fluid hatch never started such recipes even with enough stored.

## 0.19.2

### Added
* Mana, Source, Blood and Will hatches now also work as fluid tanks for recipes. A fluid input or output that matches a fluid in the hatch's fluid list is paid from or added to the hatch buffer at the configured ratio. Multiblocks no longer need a separate fluid hatch for these.

## 0.19.1

### Added
* `GTX.damageItemLabel(item)`: a display-only condition that shows the item or tag name on its own row under the durability text, so long tag names no longer run off the recipe viewer.

### Changed
* Once the durability has been taken, removing the item no longer stops the recipe. The item is only required again when the next run starts.
* Durability is now taken when the recipe starts instead of when it finishes. The mixin moved from `RecipeLogic.onRecipeFinish` to `RecipeLogic.setupRecipe`.
* The durability condition text is now "Uses N durability of:" with the name on the label row.

## 0.19.0

### Added
* `GTX.damageItem(item, amount)` recipe condition. Works on every GT machine and multiblock. The item may be an id or a `#tag`. The recipe needs a matching item with enough durability in an input slot, and takes the durability when the recipe starts. Items that run out of durability are removed. Enchantments such as Unbreaking apply.
* New mixin on `RecipeLogic` to apply the damage.
* Sanguine Synthesizer: new Alchemical Reaction Chamber mode (`sanguine_arc`), and tools in Alchemy Table recipes now lose durability instead of being consumed.

### Known limits
* Damage is applied once per started recipe, not once per parallel.

## 0.18.0

First complete build. Nothing from the earlier 0.17.x builds has been compiled or tested by the author before this one, so treat it as a release candidate.

### Added
* Blood and Will hatches for every tier from LV to UV, alongside the mana and Source hatches.
* Rotation Interface with a fixed Input or Output type, set by a button on its screen.
  * Output never accepts or loads rotation, and Input never generates it.
  * The screen shows the type, status, machine, recipe progress and EU/t, target or required RPM, network speed, su and network stress, and the reason a recipe is paused.
  * Failure messages are short so they fit GregTech's controller status panel.
* EMI and JEI recipe pages show the rotation requirement, with su and a speedometer badge, for `GTX.minRpm` and `GTX.outputRpm` recipes.
* Test scripts in `examples/test_multiblock/`, with `TESTING.txt`.

### Changed
* Shift right click no longer toggles the Rotation Interface. Right click opens the screen.
* Output interfaces refuse to start while another source already drives the Create network.
* `mods.toml` description, display URL and credits updated.
* README rewritten for every feature, with a Known issues section.

### Known issues
* The Rotation Interface can be finicky next to a regular Create gearbox.
* A controller's own screen can say it is running normally while a recipe is paused for lack of rotation.
