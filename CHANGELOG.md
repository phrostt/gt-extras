# Changelog

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
