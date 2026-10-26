<div align="center">

# GT Extras

**Mana, Source, Blood and Demonic Will for GregTech CEu Modern, a Link Tool, a Create Rotation Interface and smarter recipe pages.**

Minecraft 1.20.1 · Forge 47+ · GTCEu 7.5.x

</div>

---

## What it adds

* **Resource hatches** for Botania mana, Ars Nouveau Source, Blood Magic blood and the five types of Demonic Will, LV to UV, usable as multiblock parts and in KubeJS recipes.
* **Link Tool** that links mana and source hatches to pools, spreaders and jars, with visual feedback.
* **ME network support** for source (Ars Énergistique) and mana (Applied Botanics), and fluid and item exposure on blood and will hatches.
* **Create Rotation Interface**: one block that is set to Input (consumes rotation) or Output (generates rotation) next to a Create gearbox.
* **Recipe viewer extras** for EMI and JEI: coil and research badges, a requirement grid for the new resources, Blood Magic meteor and ritual tabs.

Every other mod is optional. A feature turns on when the mod it needs is installed.

## Requirements

| Feature | Needs |
|---|---|
| Hatches, resources, recipe cards | GregTech CEu Modern 7.5.x |
| Coil and research badges | EMI, or JEI when EMI is absent |
| Mana hatches | Botania |
| Mana on the ME network | Applied Botanics |
| Source hatches | Ars Nouveau |
| Source on the ME network | Ars Énergistique |
| Blood and Will hatches | Blood Magic |
| Meteor and ritual tabs | Blood Magic, and EMI or JEI |
| Rotation parts | Create |
| Recipes that use the resources | KubeJS |

---

## Hatches

All hatches come in **LV to UV** (for example `gtextras:mana_input_hatch_hv`) and work as multiblock parts and in KubeJS recipes. Right click a hatch to open its screen. Hatches only work while they are part of a formed multiblock, and all hatches of one kind in a multiblock share one buffer, as large as the smallest of them.

### Hatch screen

Every hatch opens a screen with the buffer bar and amount, the maximum transfer rate, the passive drain readout and the buttons below.

* **Passive operation** (ON by default). ON means the hatch also pulls and pushes by itself, up to its rate. OFF means it does nothing by itself and only moves what a running or waiting recipe asks for, at its design rate.
* **Passive drain** shows the most EU/t the hatch can draw from the multiblock's energy hatches for that, from `euPerOperation` in the config (0 by default).
* **Empty buffer** voids the buffer after a second click.
* Mana and source hatches show a list of linked targets (up to 10), an x button per target, **In order** and **Round robin** distribution, and **Clear all links**.
* Blood output hatches show a scrolling list to pick the output fluid. Will output hatches show buttons to pick the type.

### Link Tool

Mana and source hatches only use targets you link. Nothing nearby is used automatically.

| | |
|---|---|
| Recipe | Ender pearl, stick, lapis lazuli (shaped). |
| Select | Right click a mana or source hatch. |
| Link | Right click a target within 6 blocks of the hatch: a Mana Pool, Spreader or other mana receiver, or an Ars Source Jar, ME Source Jar or other source tile. Right click it again to remove the link. |
| Clear | Sneak right click on the hatch clears its links. Sneak right click in the air deselects. |
| Feedback | While you hold the tool, lines are drawn between each hatch you select and its targets. Linked hatches also show orbs while they transfer, unless `transferParticles` is off. |

The Wand of the Forest (mana) and the Dominion Wand (source) link hatches the same way. Creative pools and jars count as unlimited sources and sinks.

### Mana Input / Output Hatch
*Needs Botania.*

| | |
|---|---|
| Input | Pulls from linked Mana Pools and accepts spreader bursts. Also takes mana fluid from adjacent tanks. |
| Output | Pushes into linked pools, sparks and spreaders, or mana fluid into an adjacent tank. |
| Screwdriver | Sneak right click on the output cycles the fluids on the mana list. |
| ME network | Link the hatch to an Applied Botanics Fluix Mana Pool (it is connected to the ME network and works on the network's mana). An output hatch fills the network through it and an input hatch draws from it. Applied Botanics has no mana buses (needs Applied Botanics). |
| Capability | `GTX.MANA` |
| Ability | `GTX.MANA_INPUT`, `GTX.MANA_OUTPUT` |

| Tier | Buffer | Per tick | Per second |
|---|---|---|---|
| LV | 100,000 | 100 | 2,000 |
| MV | 200,000 | 200 | 4,000 |
| HV | 400,000 | 400 | 8,000 |
| EV | 800,000 | 800 | 16,000 |
| IV | 1,600,000 | 1,600 | 32,000 |
| LuV | 3,200,000 | 3,200 | 64,000 |
| ZPM | 6,400,000 | 6,400 | 128,000 |
| UV | 12,800,000 | 12,800 | 256,000 |

### Source Input / Output Hatch
*Needs Ars Nouveau.*

| | |
|---|---|
| Input | Pulls from linked Source Jars, ME Source Jars and other source tiles. Also takes source fluid from adjacent tanks. |
| Output | Pushes into linked source tiles, or source fluid into an adjacent tank. |
| Screwdriver | Sneak right click on the output cycles the fluids on the Source list. |
| ME network | Ars Énergistique source buses can import into and export from the hatch (needs Ars Énergistique). An output hatch fed by a bus pushes what it receives on to its links, so it works as a relay. |
| Capability | `GTX.SOURCE` |
| Ability | `GTX.SOURCE_INPUT`, `GTX.SOURCE_OUTPUT` |

| Tier | Buffer | Per tick | Per second |
|---|---|---|---|
| LV | 10,000 | 40 | 800 |
| MV | 20,000 | 80 | 1,600 |
| HV | 40,000 | 160 | 3,200 |
| EV | 80,000 | 320 | 6,400 |
| IV | 160,000 | 640 | 12,800 |
| LuV | 320,000 | 1,280 | 25,600 |
| ZPM | 640,000 | 2,560 | 51,200 |
| UV | 1,280,000 | 5,120 | 102,400 |

### Blood Input / Output Hatch
*Needs Blood Magic.*

| | |
|---|---|
| Input | Takes the blood fluids on the list from adjacent tanks, and eats blood items such as condensed blood from adjacent inventories. |
| Output | Pushes the fluid picked on the screen into adjacent tanks. |
| Pipes and buses | The hatch exposes a fluid handler, so pipes and fluid buses can fill an input hatch and drain an output hatch. Input hatches also accept the listed blood items. An item that does not fit completely is not taken. |
| Screwdriver | Sneak right click on the output cycles the blood fluid. |
| Capability | `GTX.BLOOD` |
| Ability | `GTX.BLOOD_INPUT`, `GTX.BLOOD_OUTPUT` |

| Tier | Buffer | Per tick | Per second |
|---|---|---|---|
| LV | 20,000 | 200 | 4,000 |
| MV | 40,000 | 400 | 8,000 |
| HV | 80,000 | 800 | 16,000 |
| EV | 160,000 | 1,600 | 32,000 |
| IV | 320,000 | 3,200 | 64,000 |
| LuV | 640,000 | 6,400 | 128,000 |
| ZPM | 1,280,000 | 12,800 | 256,000 |
| UV | 2,560,000 | 25,600 | 512,000 |

### Will Input / Output Hatch
*Needs Blood Magic.* Five separate buffers, one per type: raw, corrosive, destructive, vengeful, steadfast. 1 will is 1 unit.

| | |
|---|---|
| Input | Drains tartaric gems, Demon Will items, demon crystals and the extra gem items from the `willItems` config in adjacent inventories, plus will fluids from adjacent tanks. |
| Output | Pushes will fluid into adjacent tanks, or fills tartaric gems in adjacent inventories. |
| Pipes and buses | An input hatch accepts will fluids and will items. An output hatch exposes the active type as a fluid and its crystals (144 will each) as items. Tartaric gems are not accepted through the item side, since a gem's will cannot be partly taken. |
| Screwdriver | Right click on the output cycles which kind of will it outputs. Next to a tank it pushes the fluid, next to an inventory it fills tartaric gems, and drops will gems worth 144 only when the inventory has no tartaric gem at all. |
| Capabilities | `GTX.WILL_RAW`, `GTX.WILL_CORROSIVE`, `GTX.WILL_DESTRUCTIVE`, `GTX.WILL_VENGEFUL`, `GTX.WILL_STEADFAST` |
| Ability | `GTX.WILL_INPUT`, `GTX.WILL_OUTPUT` |

| Tier | Buffer | Per tick | Per second |
|---|---|---|---|
| LV | 20,000 | 200 | 4,000 |
| MV | 40,000 | 400 | 8,000 |
| HV | 80,000 | 800 | 16,000 |
| EV | 160,000 | 1,600 | 32,000 |
| IV | 320,000 | 3,200 | 64,000 |
| LuV | 640,000 | 6,400 | 128,000 |
| ZPM | 1,280,000 | 12,800 | 256,000 |
| UV | 2,560,000 | 25,600 | 512,000 |

A tartaric gem holds one type. An empty gem takes whichever type fills it first. Converting between types is done in a recipe: take one type in, give another out, and let the energy be the cost.

### Rotation Interface
*Needs Create.* A single block that uses the rotation reader texture. Place it in the multiblock next to a Create gearbox or vertical gearbox; both are normal pattern blocks and can sit anywhere the pattern allows. Each interface is either an **Output** (default) or an **Input**. Right click it to open its screen and press the type button to switch. The screen also shows the status, the controller machine, the recipe progress and EU/t, the target or required RPM, the network speed, the su provided or consumed, and the network stress. An Output never loads or accepts rotation, and an Input never generates it. Use one of each when a multiblock both needs and makes rotation.

| Recipe has | Interface does |
|---|---|
| `GTX.minRpm(n)` | Input only: loads the Create network with a stress that grows with the recipe EU/t, and the recipe runs only while the network spins at n RPM or more. |
| `GTX.outputRpm(n)` | Output only (and the recipe needs an Output interface in the multiblock): drives the network at n RPM (maximum 256), with a stress capacity that grows with the recipe EU/t. |
| the other kind of recipe | Stays idle. |

Recipe pages (EMI and JEI) show "Consumes rotation: needs N+ RPM, needs an INPUT Rotation Interface" or "Generates rotation: N RPM, needs an OUTPUT Rotation Interface" with the su involved, plus a speedometer badge, so a recipe with no item or fluid output still explains itself.
| network already driven | If the Create network is already spinning from another source when a generating recipe starts, the interface does not start generating and the screen shows Blocked. Create destroys a generator whose rotation conflicts with the network. |

| | |
|---|---|
| Block | `gtextras:rotation_interface` (recipe: Create gearbox plus HV machine casing, shapeless) |
| Pattern | `Predicates.blocks('gtextras:rotation_interface')` and `Predicates.blocks('create:gearbox', 'create:vertical_gearbox')` |
| Breaking | Wrench or pickaxe; it drops itself. |
| Screen | Right click the interface (not with a Create wrench) to open a screen with its mode (generating, consuming or idle), the network speed, the target RPM, the su it provides or loads, and the network stress bar with an OVERSTRESSED warning. |
| Orientation | The interface is a pillar block with a shaft on the two faces along its axis, and a Create gearbox has shafts on its four sides but not on the two faces along its own axis. The interface only connects when its shaft end touches a shaft face of the gearbox. Rotate either block with a Create wrench, or use a vertical gearbox, until they connect. |

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
| `.addCondition(GTX.minRpm(n))` | Loads the Rotation Interface's network and needs at least `n` RPM |
| `.addCondition(GTX.outputRpm(n))` | Makes the Rotation Interface drive the network at `n` RPM |

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

Working examples are in `examples/test_multiblock/`.

---

## Configuration

| File | Type | Content |
|---|---|---|
| `gtextras-fluids.toml` | server | Fluid lists for blood, mana, Source and the five will types, and the `willItems` list. Entries are fluid ids or `#tags`, optionally `=ratio`. |
| `gtextras-hatches.toml` | server | Storage and rate per resource and tier (rate is per tick; an existing world keeps its old values, so delete the file to pick up new defaults), `euPerOperation` (EU drawn from the multiblock's energy hatches each time the hatch moves something on its own, default 0), and `transferParticles` to turn the Ars style orbs on mana and source hatches off. |
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
