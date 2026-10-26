# GT Extras

A Minecraft Forge 1.20.1 mod for GregTech CEu Modern packs. It adds hatches for mana, Source, blood and Demonic Will, a Create rotation reader, coupler and motor, and recipe viewer extras: coil and research badges on GregTech recipe pages, a requirement grid for the new resources, and Blood Magic meteor and ritual tabs.

## Requirements

Minecraft 1.20.1 and Forge 47 or newer. Nothing else is required to load the mod.

Every other mod is optional. A feature turns on when the mod it needs is installed, and stays out of the way when it isn't.

| Feature | Needs |
|---|---|
| Hatches, resources and the extra recipe cards | GregTech CEu Modern 7.5.x (`gtceu`) |
| Coil and research badges on recipe cards | EMI, or JEI if EMI is not installed |
| Mana hatches | GregTech and Botania |
| Source hatches | GregTech and Ars Nouveau |
| Blood and Will hatches | GregTech and Blood Magic |
| Meteor and ritual tabs | Blood Magic, and EMI or JEI |
| Rotation Reader, Coupler and Motor | GregTech and Create |
| KubeJS recipes using the resources | KubeJS |

For example, a pack with GregTech and Create but no Botania gets the rotation parts and no mana hatches.

GregTech only shows coil and temperature requirements natively for the Electric Blast Furnace, Cracker and Pyrolyse Oven. Custom multiblocks registered through KubeJS show nothing, and research requirements are not shown anywhere outside the Assembly Line's own UI. The recipe viewer half of this mod fills both gaps.

## How it works

* An `EmiRecipeDecorator` adds extra slots to every matching GregTech recipe.
* **Coil requirement:** the coil list is read from the **block registry at
  runtime**, so coils added by KubeJS (`event.create('x_coil_block', 'gtceu:coil')`)
  show up automatically. The recipe's requirement is read from the recipe's own
  data tag — the value you set with `.addData("RequiredTemp", 1000)` in a KubeJS
  recipe.
* **Research requirement:** if a recipe carries a GregTech `ResearchCondition`
  (added via `.scannerResearch(...)` or `.stationResearch(...)`), the data item it
  demands (data stick, orb, or module) is drawn as a badge in the same spot as the
  coil icon. Assembly Line recipes are skipped since GregTech already shows this
  natively via the Data Access Hatch slot.
* GregTech is accessed purely by reflection, so the build has no GregTech,
  LDLib or Registrate dependency.

## JEI

If the pack runs JEI instead of EMI, the same coil and research info is drawn by a
JEI category decorator. It is registered only when EMI is not loaded (with both
installed EMI owns the recipe screen). GregTech's recipe type ids are discovered by
reflection; if one is missed, list it under `jeiRecipeTypes` in the config.
`jeiEnabled` turns the JEI side off.

## Blood Magic meteor tab

Adds a "Blood Magic Meteor" recipe tab to EMI (or JEI when EMI is absent) for every
`bloodmagic:meteor` recipe, the ones behind the Mark of the Falling Tower ritual:
the ritual name, the catalyst item, the blood (LP) the meteor costs, the ritual's
activation cost, the explosion and meteor radius, and every layer with its radius and the
blocks it can contain (fill, weighted extras with their chance, outer shell; tags cycle
through their blocks). Blood Magic's own JEI integration does not
list these. Turn it off with `meteorTab`; `meteorActivationCost` sets the activation
figure shown (250,000 LP by default). If another meteor EMI addon is installed you
will see two tabs, so disable one.

## Configuration

`config/gtextras.json` is written on first launch.

```json
{
  "enabled": true,
  "namespaces": ["gtceu", "kubejs"],
  "autoDetect": true,
  "autoDetectKeys": ["ebf_temp", "RequiredTemp", "required_temp", "coil_temp", "temperature"],
  "categories": {
    "gtceu:electric_blast_furnace": { "dataKey": "ebf_temp", "mode": "temperature" }
  },
  "showResearchIcon": true,
  "researchExcludedCategories": ["gtceu:assembly_line"],
  "offsetX": 0,
  "offsetY": 0
}
```

To wire up your own multiblock, add its recipe category id and the data key your
recipes use. `mode` picks which coil property the number is compared against:

| mode          | matched against          | used by                        |
|---------------|--------------------------|---------------------------------|
| `temperature` | coil temperature in K    | Electric Blast Furnace and co. |
| `level`       | coil level               | Multi Smelter parallels        |
| `tier`        | coil tier                | Pyrolyse Oven, Cracking Unit   |

If a category has no entry, `autoDetectKeys` is tried in order.

The research badge is controlled separately: `showResearchIcon` is a master
toggle, and `researchExcludedCategories` lists categories to skip (Assembly Line
by default - add more here if another machine ever shows its own research slot
natively).

## Building

Run `./gradlew build` (JDK 17 required). The jar lands in `build/libs/`.

## GregTech hatches

Needs GregTech CEu Modern 7.5.x. Each hatch is only registered when its mod is present. Every hatch comes in LV through UV, for example `gtextras:mana_input_hatch_hv`.

| Block | Needs | What it does |
|---|---|---|
| Mana Input / Output Hatch | Botania | Pulls from adjacent Mana Pools and accepts spreader bursts. The output pushes into pools, sparks and tanks. |
| Source Input / Output Hatch | Ars Nouveau | Pulls from and pushes into Source Jars within 6 blocks, and adjacent tanks. |
| Blood Input / Output Hatch | Blood Magic | Life Essence from and to adjacent tanks. The output also fills Blood Altars. |
| Will Input / Output Hatch | Blood Magic | Five Demonic Will types (raw, corrosive, destructive, vengeful, steadfast), each with its own buffer. |
| Rotation Reader Hatch | Create | Reads the RPM of adjacent kinetic blocks. |
| Rotation Coupler | Create | Loads the Create network while the machine next to it is working. Stress grows with the recipe EU/t. |
| Rotation Motor | Create | Spins a shaft at 64 RPM while the machine next to it is working. Stress capacity grows with the recipe EU/t. |

Storage and transfer rate per tier are set in the config. Hover a hatch to see its numbers.
Empty-hand right click on a hatch shows its contents in the action bar.

### Fluids and the screwdriver

Mana, Source, blood and will can also move as fluids. The fluid lists are in `gtextras-fluids.toml`: fluid ids or `#` tags, optionally with `=ratio` (units per mB). Input hatches take every fluid on the list. Fluids that are not installed are skipped.

- Output hatches for mana, Source and blood: sneak right click with a screwdriver to cycle the output fluid. Mana and Source also have a last entry that outputs directly into pools, jars and sparks instead of fluid.
- Will Output Hatch: sneak right click with a screwdriver to switch between pushing will fluid and filling tartaric gems in adjacent inventories.

### Will

The Will Input Hatch drains tartaric gems, Demon Will items and demon crystals from adjacent inventories (1 will = 1 unit), plus will fluids from adjacent tanks. A gem keeps its one type, and an empty gem takes the first type that fills it.

### Config files

Both are server configs, so values are per world and sync to players. Put copies in `defaultconfigs/` to set them for new worlds.

- `gtextras-fluids.toml`: fluid lists for blood, mana, Source and the five will types.
- `gtextras-hatches.toml`: storage and rate per resource and tier.

### Recipes

Parallel hatches work: the per-tick draw scales with the parallel count, and the count is limited by what the input hatches hold and the free space in the output hatches.
Crafting recipes are in `data/gtextras/recipes/` (hull of the tier, two circuits, two plates and a key block); replace them with KubeJS if you want different gating.

### KubeJS

```js
const GTX = Java.loadClass('com.gtextras.api.GTX');
```

- Capabilities: `GTX.MANA`, `GTX.SOURCE`, `GTX.BLOOD`, `GTX.WILL_RAW`, `GTX.WILL_CORROSIVE`, `GTX.WILL_DESTRUCTIVE`, `GTX.WILL_VENGEFUL`, `GTX.WILL_STEADFAST`. Use `.input(GTX.MANA, n)` or `.output(...)` for an amount taken or given at the start, and add `.perTick(true)` for a continuous draw.
- `GTX.minRpm(n)`: recipe condition for `.addCondition(...)`. Passes when a Rotation Reader Hatch of the multiblock sees at least `n` RPM.
- Part abilities for multiblock patterns, used like `Predicates.abilities(GTX.MANA_INPUT)`: `MANA_INPUT`, `MANA_OUTPUT`, `SOURCE_INPUT`, `SOURCE_OUTPUT`, `BLOOD_INPUT`, `BLOOD_OUTPUT`, `WILL_INPUT`, `WILL_OUTPUT`, `ROTATION_READER`. The coupler and motor are blocks: `Predicates.blocks('gtextras:rotation_coupler')` and `'gtextras:rotation_motor'`.

A working test multiblock and recipes are in `examples/test_multiblock/`.

### Recipe viewer

For GregTech recipes that use these resources, the card shows a small grid of icons with the amounts: red for inputs, green for outputs, `/t` for per tick, and a speedometer for the RPM needed. Hover a cell for the full numbers. In EMI, pressing R or U on the mana, Source, blood or will fluids lists the recipes that use or make them.

If a recipe cannot start because a hatch is short, the controller says which resource and how much.

### Building

Copy the mod jars listed in `libs/README.md` into `libs/` before building (Blood Magic included). Gradle must stay on 8.1.1.
