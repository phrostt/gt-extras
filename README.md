<div align="center">

# GT Extras

**Mana, Source, Blood and Demonic Will for GregTech CEu Modern, plus Create rotation parts and smarter recipe pages.**

Minecraft 1.20.1 · Forge 47+ · GTCEu 7.5.x

</div>

---

## What it adds

* **Resource hatches** for Botania mana, Ars Nouveau Source, Blood Magic blood and the five types of Demonic Will, LV to UV, usable as multiblock parts and in KubeJS recipes.
* **Create rotation parts**: a reader hatch, a coupler that loads a network and a motor that spins a shaft while the machine works.
* **Recipe viewer extras** for EMI and JEI: coil and research badges, a requirement grid for the new resources, Blood Magic meteor and ritual tabs.

Every other mod is optional. A feature turns on when the mod it needs is installed.

## Requirements

| Feature | Needs |
|---|---|
| Hatches, resources, recipe cards | GregTech CEu Modern 7.5.x |
| Coil and research badges | EMI, or JEI when EMI is absent |
| Mana hatches | Botania |
| Source hatches | Ars Nouveau |
| Blood and Will hatches | Blood Magic |
| Meteor and ritual tabs | Blood Magic, and EMI or JEI |
| Rotation parts | Create |
| Recipes that use the resources | KubeJS |

---

## Hatches

All hatches come in **LV to UV**, for example `gtextras:mana_input_hatch_hv`. Storage and transfer rate per tier are in `gtextras-hatches.toml`, and the tooltip shows the numbers. An empty hand right click shows the contents in the action bar. Hatches only work while they are part of a formed multiblock. All hatches of one kind in a multiblock share one buffer, as large as the smallest of them, so what an input hatch eats is what an output hatch can push out. On their own they do nothing, and a multiblock that is not formed stops them too.

### Mana Input / Output Hatch
*Needs Botania.*

| | |
|---|---|
| Input | Pulls from adjacent Mana Pools, accepts spreader bursts, and takes mana fluid from adjacent tanks. |
| Output | Pushes fluid into an adjacent tank. With no tank next to it, it pushes into pools, sparks and spreaders instead. |
| Screwdriver | Sneak right click on the output cycles the fluids on the mana list. |
| Wand of the Forest | Use the wand on the output hatch, then on a Mana Pool or Spreader within 6 blocks, to link them like a generating flower. A linked hatch pushes only into that target and ignores adjacent tanks, pools and spreaders. Sneak with the wand on the hatch to unlink. |
| Capability | `GTX.MANA` |
| Ability | `GTX.MANA_INPUT`, `GTX.MANA_OUTPUT` |

### Source Input / Output Hatch
*Needs Ars Nouveau.*

| | |
|---|---|
| Input | Pulls from Source Jars within 6 blocks, and from adjacent tanks. |
| Output | Pushes fluid into an adjacent tank. With no tank next to it, it fills Source Jars within 6 blocks. |
| Screwdriver | Sneak right click on the output cycles the fluids on the Source list. |
| Capability | `GTX.SOURCE` |
| Ability | `GTX.SOURCE_INPUT`, `GTX.SOURCE_OUTPUT` |

### Blood Input / Output Hatch
*Needs Blood Magic.*

| | |
|---|---|
| Input | Takes Life Essence from adjacent tanks. |
| Output | Pushes Life Essence into adjacent tanks and Blood Altars. |
| Screwdriver | Sneak right click on the output cycles the blood fluid. |
| Capability | `GTX.BLOOD` |
| Ability | `GTX.BLOOD_INPUT`, `GTX.BLOOD_OUTPUT` |

### Will Input / Output Hatch
*Needs Blood Magic.* Five separate buffers, one per type: raw, corrosive, destructive, vengeful, steadfast. 1 will is 1 unit.

| | |
|---|---|
| Input | Drains tartaric gems, Demon Will items, demon crystals and the extra gem items from the `willItems` config in adjacent inventories, plus will fluids from adjacent tanks. |
| Output | Pushes will fluid into adjacent tanks, or fills tartaric gems in adjacent inventories. |
| Screwdriver | Right click on the output cycles which kind of will it outputs. Next to a tank it pushes the fluid, next to an inventory it fills tartaric gems, and drops will gems worth 144 only when the inventory has no tartaric gem at all. |
| Capabilities | `GTX.WILL_RAW`, `GTX.WILL_CORROSIVE`, `GTX.WILL_DESTRUCTIVE`, `GTX.WILL_VENGEFUL`, `GTX.WILL_STEADFAST` |
| Ability | `GTX.WILL_INPUT`, `GTX.WILL_OUTPUT` |

A tartaric gem holds one type. An empty gem takes whichever type fills it first. Converting between types is done in a recipe: take one type in, give another out, and let the energy be the cost.

### Rotation Reader Hatch
*Needs Create.* Reads the RPM of adjacent kinetic blocks.

| | |
|---|---|
| Condition | `GTX.minRpm(n)` |
| Ability | `GTX.ROTATION_READER` |

### Rotation Coupler
*Needs Create.* A block, not a tiered hatch. Loads the Create network while the machine next to it is working. The stress it adds grows with the recipe EU/t.

### Rotation Motor
*Needs Create.* A block. Spins a shaft at 64 RPM while the machine next to it is working. Its stress capacity grows with the recipe EU/t. It copies the look of the casing around it.

---

## KubeJS

```js
const GTX = Java.loadClass('com.gtextras.api.GTX')
```

### Recipe methods

| Method | Meaning |
|---|---|
| `.input(GTX.MANA, n)` | Takes `n` at the start of the recipe |
| `.output(GTX.MANA, n)` | Gives `n` at the end of the recipe |
| `.input(GTX.MANA, n).perTick(true)` | Draws `n` every tick while running |
| `.output(GTX.MANA, n).perTick(true)` | Produces `n` every tick while running |
| `.addCondition(GTX.minRpm(n))` | Needs a Rotation Reader Hatch seeing at least `n` RPM |

`perTick(true)` applies to the input or output written just before it. Capabilities: `GTX.MANA`, `GTX.SOURCE`, `GTX.BLOOD`, `GTX.WILL_RAW`, `GTX.WILL_CORROSIVE`, `GTX.WILL_DESTRUCTIVE`, `GTX.WILL_VENGEFUL`, `GTX.WILL_STEADFAST`.

Parallel hatches work: the draw scales with the parallel count, and the count is limited by what the input hatches hold and the free space in the output hatches. Overclocking changes EU and duration only, so upfront amounts stay the same and per tick amounts are drawn for a shorter time.

```js
event.recipes.gtceu.my_machine('example')
    .itemInputs('minecraft:iron_ingot')
    .input(GTX.MANA, 50).perTick(true)
    .input(GTX.WILL_RAW, 144)
    .output(GTX.WILL_CORROSIVE, 144)
    .addCondition(GTX.minRpm(64))
    .duration(100)
    .EUt(GTValues.VA[GTValues.LV])
```

### Multiblock abilities

Used in a pattern like `Predicates.abilities(GTX.MANA_INPUT)`.

| Ability | Hatch |
|---|---|
| `GTX.MANA_INPUT`, `GTX.MANA_OUTPUT` | Mana hatches |
| `GTX.SOURCE_INPUT`, `GTX.SOURCE_OUTPUT` | Source hatches |
| `GTX.BLOOD_INPUT`, `GTX.BLOOD_OUTPUT` | Blood hatches |
| `GTX.WILL_INPUT`, `GTX.WILL_OUTPUT` | Will hatches |
| `GTX.ROTATION_READER` | Rotation Reader Hatch |

The coupler and motor are plain blocks: `Predicates.blocks('gtextras:rotation_coupler')` and `Predicates.blocks('gtextras:rotation_motor')`.

Working examples are in `examples/test_multiblock/`.

---

## Configuration

| File | Type | Content |
|---|---|---|
| `gtextras-fluids.toml` | server | Fluid lists for blood, mana, Source and the five will types, and the `willItems` list. Entries are fluid ids or `#tags`, optionally `=ratio`. |
| `gtextras-hatches.toml` | server | Storage and rate per resource and tier, `euPerOperation` (EU drawn from the multiblock's energy hatches each time the hatch moves something on its own, default 0), and `transferParticles` to turn the Ars style orbs on mana and source hatches off. |
| `config/gtextras.json` | client | Coil and research badge settings. |

Server configs are per world and sync to players. Put copies in `defaultconfigs/` to set them for new worlds. Fluids that are not installed are skipped.

---

## Recipe viewer

GregTech only shows coil and temperature requirements natively for the Electric Blast Furnace, Cracker and Pyrolyse Oven. Custom multiblocks registered through KubeJS show nothing, and research requirements are not shown outside the Assembly Line's own UI. The recipe viewer half of this mod fills both gaps.

* **Coil requirement:** the coil list is read from the block registry at runtime, so coils added by KubeJS (`event.create('x_coil_block', 'gtceu:coil')`) show up automatically. The requirement is read from the recipe's own data tag, the value set with `.addData("RequiredTemp", 1000)`.
* **Research requirement:** a recipe with a GregTech `ResearchCondition` (`.scannerResearch(...)` or `.stationResearch(...)`) shows the data item it demands (data stick, orb or module) next to the coil icon. Assembly Line recipes are skipped since GregTech already shows this.
* **Resource grid:** mana, Source, blood, will and RPM requirements are drawn as a grid of cells, red for inputs and green for outputs, with `/t` for per tick amounts. Hover a cell for the full number. The cells also work as recipe lookups (R and U) on EMI and JEI.

### JEI

When EMI is not installed the same info is drawn by a JEI category decorator. GregTech's recipe type ids are discovered by reflection; list any that are missed under `jeiRecipeTypes`. `jeiEnabled` turns the JEI side off.

### Blood Magic meteor tab

Adds a "Blood Magic Meteor" tab to EMI (or JEI) for every `bloodmagic:meteor` recipe, the ones behind the Mark of the Falling Tower ritual: the ritual, the catalyst item, the blood the meteor costs, the activation cost, the explosion and meteor radius, and every layer with its radius and blocks. Turn it off with `meteorTab`; `meteorActivationCost` sets the activation figure shown (250,000 LP by default). If another meteor EMI addon is installed you will see two tabs, so disable one.

### Badge configuration

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

To wire up your own multiblock, add its recipe category id and the data key your recipes use. `mode` picks which coil property the number is compared against:

| mode | matched against | used by |
|---|---|---|
| `temperature` | coil temperature in K | Electric Blast Furnace and co. |
| `level` | coil level | Multi Smelter parallels |
| `tier` | coil tier | Pyrolyse Oven, Cracking Unit |

If a category has no entry, `autoDetectKeys` is tried in order. `showResearchIcon` is a master toggle for the research badge, and `researchExcludedCategories` lists categories to skip.

---

## Building

JDK 17. Put the dependency jars in `libs/` (see `libs/README.md`), then run `./gradlew build`. The jar lands in `build/libs/`.
