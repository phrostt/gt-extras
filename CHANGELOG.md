# Changelog

## 0.21.3

### Fixed
* **Research icons no longer cover the tier label.** On research cards such as the Prototype Assembler, the research and target icons and the arrow between them sit 7 pixels higher, the same lift the coil icon got, so the blueprint no longer overlaps the tier label (LuV, ZPM and so on).

## 0.21.2

### Changed
* **Coil icon lines up with the tier label.** The coil icon sits 3 pixels further right, so its right edge matches the right edge of the tier label (IV, ZPM and so on).
* **Long names on cards fit.** The right edge limit for the `bridge in` and `bridge out` lines was too strict and cut names such as `gtceu:bose_einstein_condensate` with a `~` even though they fit. The limit is now at the card edge, so only names that really run past it are shortened.

## 0.21.1

### Fixed
* **Bridge lines line up with the card text.** The `bridge in` and `bridge out` lines now start at the same left edge as the native lines (Duration, Total, Usage) instead of sitting a few pixels to the right, and long names are cut at 30 characters so they stay inside the card.
* **Coil icon no longer covers the tier label.** On cards that draw the coil icon themselves, the icon now sits 7 pixels higher, so its bottom lines up with the bottom of the temperature line and it no longer overlaps the tier label (ZPM, UV and so on) at the bottom right.
* **Yield bonus applies by chance on small packets.** The bonus used to be rounded to the nearest whole packet, so a packet of 1 only became 2 at a bar that was exactly full, which never happens while resonance decays. The leftover part of the bonus is now a probability, so a packet of 1 at a 20% bonus becomes 2 one time in five and the average output follows the bonus exactly.
* **Resonance decay matches the setting.** Decay used to lose at least 1 every half second, which doubled a setting of 1 per second. It now follows the configured rate exactly, including fractions of a point.
* **Coil text and icon spacing.** The temperature and coil lines now start at the same left edge as the native card text, and the coil icon sits 4 pixels further right, so the text no longer runs into the icon. The bridge lines moved 2 pixels further left to match exactly. Lines that would run past the card edge are shortened with a `~` instead of being cut off by the border.

### Changed
* Version bumped so the jar is easy to tell from earlier 0.21.0 test builds.
* **Build.** The compile-time copies of the pack jars from `libs/` are now staged in `.staged-libs/` instead of `build/staged-libs/`, so `gradlew clean build` no longer deletes them and fails with "Could not find local:...". `.staged-libs/` is ignored by git.

## 0.21.0

### Added
* **Wireless Energy bridge.** A second built-in bridge, `wireless_energy`, with its own 16 hatches (`gtextras:wireless_energy_input_hatch_<tier>` and `gtextras:wireless_energy_output_hatch_<tier>`, LV to UV), amber overlay textures, models, names and crafting recipes (hull, circuits and plates around a block of redstone). `einstein_rosen` stays the resonance bridge and has energy turned off by default.
* **Wireless energy through bridge hatches.** Hatches of a bridge with energy enabled now carry energy. An output hatch accepts EU from adjacent cables or machines at its tier voltage and sends it along its links to input hatches of the same bridge. An input hatch pushes the energy it receives into adjacent cables or machines at its tier voltage. No recipe is needed.
* **Energy settings per bridge.** A new optional `energy` block in `gtextras_bridges.json` with `enabled` (true), `range` (64 blocks), `lossPercent` (0), `maxAmperage` (0, no cap) and `bufferTicks` (20). A bridge without an `energy` block uses these defaults, and `"enabled": false` turns energy off for that bridge.
* **Energy on the hatch screen.** An energy bar showing stored and maximum EU appears on every bridge hatch screen when energy is enabled.
* **Hatch tooltips** describe the energy behaviour and the link range.
* **New default configuration.** The generated default bridge file now holds every setting: `einstein_rosen` with black and white beams, a resonance cap of 10,000, UV hatches and `#gtceu:circuits/uxv` as the hatch item, and `wireless_energy` with UV hatches, `#gtceu:circuits/opv` as the hatch item and the full `energy` block with amperage ratings 1, 2, 4, 8 and 16.
* **Energy buffer setting.** `energy.bufferSize` sets the exact buffer of an energy hatch in EU, 0 for automatic. A hatch with a full buffer accepts no more energy.
* **Amperage ratings.** `energy.amperages` takes any of 1, 2, 4, 8, 16. Each tier then gets a hatch per rating, named `<bridge>_<input|output>_hatch_<tier>_<n>a`. The rating limits the amps in and out and sizes the buffer. Models, blockstates, names and crafting recipes for all ratings and tiers of `wireless_energy` are included. A higher rating is crafted from two hatches of half the rating and the bridge's `hatchItem`.
* **Tier matching for energy.** Energy only flows between hatches of the same tier, so a bridge cannot act as a free voltage transformer. `energy.allowTierMismatch` turns this off. Packets are not affected.
* **Documentation.** A new reference, `docs/BRIDGES.md`, covering hatches, linking, packets, resonance, wireless energy, every config field, recipes, the KubeJS API, worked examples, how to make a new bridge, and troubleshooting. The README is updated to match.

### Changed
* **Hatch recipes only load when the hatch exists.** Every bridge hatch recipe now has an item exists condition, so hatches removed by `tiers` or `amperages` no longer produce recipe errors in the log.
* **Configurable hatch recipe item.** Each bridge has a `hatchItem` setting in `gtextras_bridges.json`, the item placed above the hull in the crafting recipe of its hatches. It defaults to the Eye of Ender and the built-in `wireless_energy` bridge uses a block of redstone. The crafting recipes use a new ingredient type, `gtextras:bridge_item`, that reads the setting when recipes load. It takes an item id, including one registered by KubeJS, or an item tag written with a leading `#`, such as `#gtceu:circuits/uxv`.
* **Packet amount is optional.** `'einstein_rosen item gtceu:tesseract'` is the same as `'einstein_rosen item gtceu:tesseract 1'`. A packet with a missing or extra part is still rejected.
* **Selectable hatch tiers.** A bridge can list the tiers it registers, for example `"tiers": ["mv", "hv"]`, to gate its hatches. A missing or empty list registers LV to UV as before, and the generated default config lists only `uv` for both built-in bridges, and an unknown tier name is logged and skipped.
* The Link Tool range for a bridge is now the larger of `range` and `energy.range`. Packets still travel only within `range`.
* Version and README updated for 0.21.0.

### Fixed
* **A full output hatch no longer stops recipes.** When a bridge output hatch has no room for a packet a recipe produces, for example resonance when the hatch is already full or has nowhere to send it, the hatch stores what fits and discards the rest. The recipe used to fail with "Insufficient Outputs".
* **Stale card heights after a reload.** The extra card height measured for each recipe type is now cleared whenever the client receives updated recipes, so cards for recipe types whose recipes change on a script reload (the black and white hole) are measured again instead of keeping a height from before the reload. The log line `card extra` now also shows the recipe count.
* **Bridge capability name.** Machine status messages showed `recipe.capability.bridge.name`. They now show "Bridge".

### Notes
* A hatch only accepts energy at or below its tier voltage, so a higher voltage is refused.
* Both ends of a link must be in loaded chunks for energy to flow.
* Energy is not a recipe packet kind. Recipes cannot read or produce it.

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
