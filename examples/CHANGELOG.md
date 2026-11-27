# Changelog

## 0.45

### Added
* **A stage can pay several items and fluids.** The success and fail arguments of `.stage(...)` now also take a list, for example `.stage(100, ['2x alltheores:lead_plate', 'minecraft:water 500'], false, null)`. Every entry in the list is paid out together (each is added to the recipe's outputs for JEI and the room check as well). A single string or `null` still works as before. Note that the machine's output limits apply, for example the chemical reactor takes 2 item outputs.
* The stage line in JEI shows the first item and `+N` for the rest.

### Changed
* Version moved to 0.45.

## 0.44

### Changed
* **Stage item syntax follows KubeJS.** An item is `'2x gtceu:lead_plate'` or just `'gtceu:lead_plate'` (count omitted means 1). A fluid is the id followed by an amount, `'minecraft:water 1000'`; the amount cannot be omitted, and the number after the id is what marks it as a fluid. The `fluid:` prefix from 0.41 to 0.43 is gone.
* Version moved to 0.44.

## 0.43

### Changed
* **Stage item strings accept the amount before or after the id.** `'1x gtceu:lead_plate'` and `'gtceu:lead_plate 1'` now both work (fluids too: `'fluid:minecraft:water 1000'` or `'fluid:1000x minecraft:water'`). Before, only the second form was read correctly.
* Version moved to 0.43.

## 0.42

### Fixed
* **"Format error" on the stage lines in JEI.** The stage text contained a bare `%`, which GregTech's label formatter reads as a format code. The percent sign is now escaped, and a stage with no item reads `1: 75% retry` instead of showing a dash.

### Changed
* Version moved to 0.42.

## 0.41

### Added
* **Staged recipes: `.stage(chance, successItem, repeat, failItem)`.** A new method on every GregTech KubeJS recipe, for any machine. Call it once per stage; stages run in the order written. The recipe's duration and EUt are the cost of one attempt at a stage, and the recipe's inputs are taken once, when the recipe starts.
  * `chance` is a percent from 0 to 100. 100 is guaranteed.
  * `successItem` is paid out when the roll succeeds, then the recipe moves to the next stage; after the last stage the recipe is done. Use `null` for nothing. An item is `'modid:item count'`; a fluid is `'fluid:modid:fluid amount'`.
  * `repeat` true: a failed roll pays nothing and the same stage is tried again. false: a failed roll ends the recipe and pays `failItem` (or nothing for `null`).
  * One roll per stage per batch, shared by all parallel copies, so parallels always stay on the same stage and the payout is multiplied by the parallel count.
  * Stage progress is saved with the machine, so a recipe resumes on the same stage after a world reload, and it pauses and resumes with power like any recipe.
  * Stage items are the recipe's only outputs. They are also added as chanced outputs (cumulative chance of ever receiving that item) so JEI shows the percentages and the machine will not start without room for them. GregTech's own output roll is skipped for staged recipes.
* **Stage list in JEI.** Each stage is listed under the recipe text as `1: 75% lead plate r` (an `r` marks repeat on fail). The card grows by one line per stage.
* **Stage readout on the machine.** The recipe progress line shows `Stage 2/5`.
* **Test scripts** with a 5 stage chemical reactor recipe (water and lead ingot in, lead plate out on stage 5) and examples of guaranteed, repeating and risky stages.

### Changed
* Version moved to 0.41.

### Known gaps
* Untested in game. The Stage readout is the machine progress line; The One Probe is not wired separately.
* If the output is full when a stage pays out, that stage's item is lost; room is only checked when the recipe starts.

## 0.40

### Changed
* **Version moved to 0.40.**
* **Removed the per-category diagnostic log line** added in 0.38 ("native widgets added by GregTech..."). Nothing is written to the log for the icon row any more, apart from a warning if moving the native coil slot throws an error.

## 0.38

### Fixed
* **Native coil icon is now moved at the moment GregTech adds it.** Earlier versions searched the recipe widget afterwards for GregTech's coil slot and never matched it. The mod now hooks GregTech's own step that adds the category's extra widgets (`appendJEIUI`), takes the slots that step added, and moves the coil one into the coil box straight away, with its frame behind it. If the mod draws its own coil icon for the recipe, GregTech's is hidden instead. Slots GregTech did not add in that step are never touched.

### Added
* **Log line per category.** The first time a category with the icon row is built, the log shows how many widgets GregTech added and whether a coil slot was found (`native widgets added by GregTech: N, slots: N, coil slot: true/false`), so a failure can be diagnosed from `latest.log`.

## 0.37

### Fixed
* **The real cause of the Electric Blast Furnace coil icon not moving.** GregTech's slot widget defaults its ingredient role to render-only, never to none. The check added in 0.34 required no role at all, so it could never match GregTech's coil slot, and every version since then silently skipped it. The coil slot is now matched when it holds a coil block, or when its role is render-only (or unset) and it sits in the lower right of the card. Slots with an input, output or catalyst role, and slots named item, fluid or research, are still never touched.

## 0.36

### Fixed
* **Electric Blast Furnace coil icon is now found.** GregTech builds a card's slots inside a nested child group of the recipe widget, and 0.30 to 0.35 only searched the top level, so GregTech's coil slot was never seen. The search now goes through nested groups and works out the slot's position inside its own group. The coil slot is moved into the coil box and its frame is added in the same group, directly behind it, so the frame no longer covers the icon.

## 0.35

### Fixed
* **Electric Blast Furnace coil icon moves into the coil box regardless of where GregTech put it.** 0.30 to 0.34 only looked for GregTech slots that overlapped the icon row, but GregTech's coil slot sits just above it, so it was never found. The mod now finds the coil slot by what it is (a GregTech slot with no ingredient role that holds a coil block, or failing that one in the lower right of the card), takes it out of GregTech's position and puts it, framed, in the coil box. Item input, fluid input and research slots are still never touched.

## 0.34

### Fixed
* **Electric Blast Furnace coil icon now actually moves into the row.** GregTech's native coil slot is GregTech's own slot widget subclass, which the 0.30 to 0.33 filter did not match, so the coil icon stayed above the voltage badge. The mod now recognises it as a GregTech slot with no ingredient role (item input, fluid input and research slots always have one), and moves it into the coil box with a frame.

## 0.33

### Changed
* **Voltage box is now a tier badge.** The tier name (LV, HV, ZPM, LuV, UHV, ...) is drawn by the mod on a coloured gradient tile, one colour per tier, with the text scaled down when needed, so the long names no longer overflow the box. GregTech's own text label is hidden; its click-to-step-overclock button is kept over the box, and the badge reads the current tier from GregTech's widget every frame.

## 0.32

### Fixed
* **Only the Assembly Line keeps GregTech's research slot.** 0.31 skipped the research box for every recipe type GregTech flags as having a research slot, which also caught other assembler-style machines. Now only `gtceu:assembly_line` is skipped. Every other category with research, such as a prototype assembler, gets the research box in the icon row again.

## 0.31

### Fixed
* **Assembly Line research stays GregTech's own.** Recipe types with GregTech's dedicated research slot no longer get a research box in the icon row at all (JEI and EMI), so there is no duplicate and nothing near GregTech's research slot is touched.

## 0.30

### Fixed
* **GregTech's own slots are left alone, including the Assembly Line research slot.** The 0.29 overlap fix could grab any slot near the icon row. It now only moves GregTech's native coil icon (the plain cycling coil slot on the Electric Blast Furnace and similar). Item input, fluid input and research slots are never moved or hidden, so the Assembly Line's dedicated research slot stays exactly where GregTech puts it.

## 0.29

### Fixed
* **Electric Blast Furnace (and other categories with GregTech's own coil icon) no longer overlap the icon row.** GregTech places its own coil slot near the bottom right of those cards, which landed on top of our circuit and voltage boxes. The mod now finds any GregTech slot that overlaps the row and moves it into the coil box, with a matching frame, so the native coil icon becomes the coil box of the row. If the mod already shows its own coil icon for the recipe, GregTech's duplicate is hidden instead.

## 0.28

### Changed
* **Icon row at the bottom right of the recipe page.** Research, coil, circuit and voltage now sit in one row of four equal boxes, left to right in that order, aligned to the bottom right of the card. In 0.27 the circuit was a badge in the badge grid, which added a whole extra row and made the card much taller. It is now a box in the icon row, so the only extra height is one row of 20 pixels (plus any text lines and badges the category already had). A box is only drawn when the recipe has that info, and the voltage box is always the rightmost.
* **Voltage label moved into the last box.** GregTech's own tier label (EV, HV, ...) is moved into the fourth box and centred, with GregTech's click-to-step-overclock button resized to match, so the overclock control still works. This only happens in categories that have the icon row.
* **Research is one box.** The research data item is shown in the research box. If the research is for a specific item, that item is named in the tooltip; the separate target icon and arrow are gone. Research and coil no longer share a position, so a recipe with both shows both.
* **Frames use GregTech's slot texture**, added to the recipe widget under the voltage label, so the boxes match GregTech's own slots. EMI slots in the row have their own background turned off to avoid a double frame.
* **Card height.** A category's extra height is now the text and badge height plus one 20 pixel row when any of its recipes has research, a coil or a circuit, instead of the larger of the two.
* **JEI fallback tooltip** over the row now covers the coil, research and circuit boxes separately.
* **README documents the icon row** in a new section replacing "Programmed circuit badge".
* **Version bumped to 0.28.**

### Fixed
* **The circuit no longer adds a badge row.** The circuit is no longer part of the badge grid, so it does not grow the card by a badge row or push the other badges down.

## 0.27

### Added
* **Programmed circuit badge.** GregTech shows a recipe's programmed circuit as an item input. A recipe that already uses every item slot of its machine (three inputs on a Large Chemical Reactor, for example) had no slot left for it, so the circuit was missing from the recipe page. The circuit is now removed from the item input slots and drawn as the first badge in the badge grid, as the circuit item with its configuration number, with the tooltip "Programmed circuit" and "Set the machine's circuit to N". This applies to every GregTech recipe type on both EMI and JEI, and needs no change to any recipe.
* **Circuit badge is a recipe lookup in JEI.** On JEI the badge is registered as an input slot, so pressing R or U on a circuit still lists the recipes that use it, as it did when the circuit sat in an item slot.
* **`showCircuitBadge` setting.** New option in `config/gtextras.json` (default `true`) to turn the circuit badge off. It is also off when `enabled`, `showExtraBadges` or the active viewer's integration (`jeiEnabled`, or EMI) is off, and the circuit then stays in its slot as before.
* **New lang keys** `gtextras.tooltip.badge.circuit` and `gtextras.tooltip.badge.circuit_config`.

### Changed
* **`GtRecipeWidgetMixin` filters the circuit out of the item input list.** A new `@ModifyArg` on the first `Table.put` in `GTRecipeWidget.collectStorage` hands GregTech a copy of the input list without the circuit. The recipe itself is never changed, only the list the viewer lays out. It is `require = 0`, so if a GregTech update moves that call the circuit simply shows in its slot again.
* **Badge grid grows by one row** for categories whose recipes use a circuit, and the circuit badge sorts before the mana, source, blood, will and rotation badges.
* **README documents the circuit badge** in a new "Programmed circuit badge" section, the feature list, the requirements table and the badge configuration.
* **Version bumped to 0.27.**

## 0.26

### Added
* **Forestry individual ingredient (`gtextras:forestry_individual`).** A recipe ingredient that names a Forestry species and life stage, `{ "type": "gtextras:forestry_individual", "species": "forestry:bee_pirate", "stage": "drone" }`, with an optional `species_type` (default `forestry:bee`). A Forestry bee keeps its genome in capability data, and Forge writes only the plain item NBT when GregTech copies, saves or sends a recipe, so a drone named with `forge:nbt` ended up as a Forest drone in a GregTech recipe. This ingredient is rebuilt from Forestry's own API (`createStack` of the species type) each time its stacks are needed, is saved to JSON as the three ids, and is sent to the client as the three ids, so the server, the saved recipe and the client all build the same real bee. `test` accepts only a stack of that species and stage. It is registered only when Forestry is loaded.
* **Forestry as an optional dependency.** `mods.toml` lists Forestry as optional, loaded before this mod, and `libs/README.md` lists `forestry-1.20.1-2.11.2.jar` as a compile-time jar to copy into `libs/`.

### Changed
* **README documents the ingredient.** A new "Forestry individual ingredient" section covers the fields, the stage values and why it exists, and the feature list and requirements table mention it.
* **Version bumped to 0.26.**

## 0.25

### Added
* **Alt + left click copies the item id of any recipe slot.** On JEI's recipe screen, Alt + left click on any slot of a recipe, input, output or catalyst, copies the registry id of the item shown there (for a fluid slot, the fluid's id) to the clipboard and prints "Copied item id" in chat. Shift + left click still copies the recipe id. Both use the same `copyRecipeId` setting in `config/gtextras.json`. The new `gtextras.copied_item_id` lang key is added.

### Changed
* **README documents the JEI id copy.** A new "Copy ids from JEI" section describes Shift click, Alt click, the copy key and the config setting.

## 0.21.27

### Changed
* **Sterile Cleaning Maintenance Hatch recipe is now a full 3x3.** The pattern is circuit, casing, circuit / pump, hatch, pump / circuit, casing, circuit, so it takes 4 UV circuits, 2 Sterilizing Filter Casings, 2 UV Electric Pumps and a Cleaning Maintenance Hatch (it was a 2-row recipe with 2 casings, 1 circuit and 2 pumps).

## 0.21.26

### Added
* **Sterile Cleaning Maintenance Hatch.** A UV maintenance hatch that gives the multiblock it is part of a Sterile Cleanroom. GregTech 7.5.3 has the Sterile Cleanroom type but only registers the plain Cleaning Maintenance Hatch. The new hatch also counts as a plain Cleanroom, so Cleanroom recipes run on it too, and it maintains the multiblock automatically like the plain hatch. It comes with a model, a green overlay, a crafting recipe, language entries and the wrench and pickaxe tags.

### Removed
* **Converter push log line.** The `converter at ... pushing into pipe network` message that was printed every few seconds for each converter is gone, along with the counters that only fed it.

## 0.21.25

### Changed
* **Converter push into Pipez rewritten.** A Pipez pipe that has no Extract face or upgrade has no block entity, so it could not take a push and had no destination list; every earlier hook on the pipe's block entity had nothing to attach to. A GT converter in EU to FE mode whose front is a Pipez energy pipe now finds the connected energy pipes itself and delivers into every block on that network that accepts Forge Energy, with no per-face transfer limit, spending EU from its buffer at the normal ratio (limited only by the converter's own voltage and amps). A pipe face set to Extract is left to Pipez, so pulling still works. The earlier push, capability, rate and connection hooks and the diagnostic probes are removed.

## 0.21.24

### Added
* **Converter probe.** Every few seconds each GT converter logs its mode, front direction, what block is in front of it, whether that block gives an energy capability and whether it can receive. To be removed once converter push is working.

## 0.21.23

### Changed
* **Pipe hooks fail loudly again, with a heartbeat log.** The Pipez push and capability hooks stop the game at load if a target does not match, and the log prints (throttled) when a pipe is asked for its energy capability, so a hook that is not applying can be told apart from one that is not reached.

## 0.21.22

### Added
* **Converter to pipe diagnostics.** The log now shows when a pipe is asked for its energy capability by a GT machine next to it, what that machine is, the converter's front and mode, and when the capability is handed to a converter. To be removed once converter push is confirmed.

## 0.21.21

### Changed
* **Back to the 0.21.14 Pipez push.** The Pipez push mixins are restored to exactly their 0.21.14 form (capability on the converter-facing face, push accepted on any face, no rate cap, source face skipped), and the connection-cache hook, converter helper, buffer and loud-failure settings from 0.21.15 to 0.21.20 are removed. One log line is added where a push reaches the pipe, showing the face's own rate.

## 0.21.20

### Changed
* **Pipez hooks now fail loudly.** The three Pipez mixins no longer skip silently when a target does not match; the game stops at load and names the mismatch. A line is also logged when a pipe next to a converter is counted as a source.

## 0.21.19

### Fixed
* **Converter push into a pipe now has somewhere to go.** Pipez only works out a pipe's destinations when the pipe has an Extract face, so a pipe taking a converter's push with no Extract face had an empty destination list and the energy was never delivered. A pipe touching the output of a converter in EU to FE mode is now treated as a source for that, so its destinations are found. The buffer (0.21.16) and the diagnostic logging (0.21.15, 0.21.18) are removed; they were built on a wrong diagnosis.

## 0.21.18

### Added
* **Pipe push diagnostics.** The log prints (throttled) when a pipe face is offered to a converter, when a converter next to a pipe is not used and why, and what a push offers, delivers and buffers. To be removed once converter push into pipes is confirmed working.

## 0.21.17

### Fixed
* **Buffered push no longer touches the pipe class.** 0.21.16 stored the buffer in a field added to Pipez's pipe and drained it from a hook in the pipe's tick; push stopped working. The buffer now lives in a separate table and is drained from a world tick event, and the pipe mixin is back to only handing out the capability. The diagnostic log line from 0.21.15 is removed.

## 0.21.16

### Changed
* **Buffered push into a pipe.** A converter pushing into a non-Extract pipe face is no longer held to what the pipe's destinations take in that single tick. Energy that cannot be delivered at once waits in a buffer on that face (up to 2,147,483,647 FE) and the pipe feeds it onward every tick, like a Mekanism cable. The buffer is not saved across a world unload.

## 0.21.15

### Added
* **Pipez push logging.** While a non-Extract pipe face is being pushed into, the log prints a line every 200 pushes with the pipe position, face, the FE offered and the FE accepted. This is a diagnostic for finding what limits a converter's output into a pipe, and will be removed once that is found.

## 0.21.14

### Changed
* **Unlimited push into a non-Extract pipe face.** A pipe face that takes a converter's push is no longer limited by Pipez's per-face transfer rate or upgrade (1024 FE/t with no upgrade). It accepts whatever the converter offers; the pipe's other faces and Extract faces keep their normal Pipez rates.

## 0.21.13

### Fixed
* **Pipez no longer affected anywhere except at a converter.** 0.21.9 to 0.21.12 gave every energy pipe an energy capability on every non-Extract face and changed how Extract faces take pushes, which could disturb ordinary Pipez networks. The capability is now only added on a pipe face that touches the front of a GregTech converter running in EU to FE mode, and the push check no longer rejects Extract faces. Everything else behaves exactly as Pipez ships.

## 0.21.12

### Changed
* **Converter pull rewritten.** The mixin that lets a Pipez Extract face pull from a GregTech converter in EU to FE mode (0.21.11) was replaced by an independent implementation (`ConverterOutputSideMixin`). It behaves the same: in EU to FE mode the converter's Forge Energy side can be drained at the normal ratio from its EU buffer, and in FE to EU mode nothing changes.

## 0.21.11

### Added
* **Pipes can pull from GregTech energy converters.** A converter in EU to FE mode reported that its Forge Energy side cannot be extracted from, so a Pipez Extract face (or any other puller) got nothing. That side can now be drained in EU to FE mode, taking EU from the converter's buffer and giving FE at the normal ratio. In FE to EU mode it still only accepts energy. Together with 0.21.10 this means a non-Extract pipe face takes the converter's push and an Extract face pulls, each through its own face. Pipez's transfer rate and upgrades still apply. If you also run the GT Pipez Compat mod, the two give the same result, so you can remove it.

## 0.21.10

### Changed
* **Pipez energy push only on non-Extract faces.** A face of a Pipez energy pipe now accepts a push only when it is not set to Extract. In 0.21.8 and 0.21.9 every face accepted one. An Extract face goes back to being pull-only, so it no longer takes pushes (for example from a GregTech converter), and it can be used with a mod that lets pipes pull from converters without the two paths adding up on the same face.

## 0.21.9

### Fixed
* **Pipez energy push, second half.** 0.21.8 let `EnergyPipeType.receive` accept a push on any face, but Pipez also hides an energy pipe's Forge Energy capability on every face that is not set to Extract, so a GregTech converter never even found the pipe to push into. A new mixin on `PipeLogicTileEntity.getCapability` now hands out the energy capability on those faces too. The storage it gives out is never ticked, so a non-extract face still never pulls energy out of its neighbour.

## 0.21.8

### Added
* **GregTech energy converters can push into Pipez energy pipes.** Pipez only accepted energy pushed into a face set to Extract, so a converter in EU to FE mode could not feed a pipe. A new mixin on Pipez's `EnergyPipeType.receive` lets a face of an energy pipe accept a push, the way Mekanism cables do, and leaves the face being pushed into out of the destinations so energy never loops back. Pipez's per-pipe transfer rate (upgrades and the `energyPipeAmount*` server config values) still caps how much goes through. Does nothing if Pipez is not installed. Building needs `pipez-forge-1.20.1-1.2.26.jar` in `libs/`.

## 0.21.7

### Fixed
* **Fluid tooltips in the JEI ingredient list, for real this time.** The mixin approach from 0.21.5/0.21.6 had no visible effect, so it is removed. A client event handler on Forge's tooltip gather event now asks the JEI runtime which fluid is under the mouse (ingredient list, bookmarks or recipe screen) and inserts GregTech's own fluid lines (formula, state, temperature, cryogenic note, attributes, Shift ingot conversion) right after the name. It does not depend on JEI internals, and skips fluids whose tooltip already contains the GT lines, so GregTech's own recipe widgets are not doubled. Needs JEI and GTCEu; items were not affected.

## 0.21.6

### Changed
* **JEI fluid tooltip hook now logs.** The hook from 0.21.5 failed silently. It now writes one line to `latest.log` the first time it runs (`[GT Extras] JEI fluid tooltip hook ran for <fluid> and added <n> lines`) and one error with the stack trace if GregTech's tooltip code throws, so a missing tooltip can be traced.

## 0.21.5

### Fixed
* **Fluid tooltips in the JEI ingredient list.** Hovering a fluid, gas or plasma in the JEI ingredient list only showed its name, because GregTech adds its fluid lines in its own widgets and not in JEI's list. A new client mixin on JEI's fluid tooltip (`mezz.jei.forge.platform.FluidHelper.getTooltip`) now calls GregTech's own fluid tooltip handler after JEI builds the tooltip, so the chemical formula, state, temperature, cryogenic note, fluid attributes and the Shift ingot conversion show up. Items were not affected. GregTech's own recipe pages are unchanged, so lines are not doubled there.

## 0.21.4

### Fixed
* **Full item names fit on the bridge lines.** The right edge limit was still a few pixels too strict, so `gtceu:bose_einstein_condensate` lost its last letters. The limit is now wide enough for the full name on a standard card.

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
