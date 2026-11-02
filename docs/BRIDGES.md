# Bridges

Complete reference for the bridge system in GT Extras: packet hatches, resonance, wireless energy, the definition file, the KubeJS API, recipes, assets for new bridges, and troubleshooting. The README has the short version of everything here.

Everything in this file describes version 0.21.3. Bridges were added in 0.20.0 and wireless energy in 0.21.0.

---

## 1. Concept

A bridge is a named link between multiblocks. Nothing physical travels along it. A machine sends **packets** through an **output hatch**, the packets arrive in an **input hatch** on another machine, and recipes on both ends decide what goes in and what comes out. A packet is only a kind, a name and an amount. The receiving recipe can turn it into anything.

The same hatches also carry **energy**, when the bridge has energy turned on, without any recipe.

Every bridge is defined in a JSON file. Each definition produces its own hatches (an input and an output per tier), its own KubeJS abilities, and its own link channel. A hatch of one bridge never links to a hatch of another.

Two bridges ship with the mod:

| Bridge | Purpose | Colour | Energy | Default tiers |
|---|---|---|---|---|
| `einstein_rosen` (Einstein-Rosen Bridge) | Packets and resonance | Black, white return | Off | UV |
| `wireless_energy` (Wireless Energy) | Wireless power | Amber | On | UV |

---

## 2. Hatches

### 2.1 Identifiers

| Hatch | Identifier |
|---|---|
| Plain | `gtextras:<bridge>_input_hatch_<tier>` and `gtextras:<bridge>_output_hatch_<tier>` |
| With an amperage rating | `gtextras:<bridge>_input_hatch_<tier>_<n>a` and `gtextras:<bridge>_output_hatch_<tier>_<n>a` |

`<tier>` is one of `lv`, `mv`, `hv`, `ev`, `iv`, `luv`, `zpm`, `uv`. `<n>` is 1, 2, 4, 8 or 16.

Which tiers exist is controlled by the `tiers` setting. A bridge with energy on and an `amperages` list registers only the rated hatches. Without an `amperages` list it registers the plain hatches.

### 2.2 Output hatch (sender)

- Holds packets produced by a recipe until they have been sent.
- Sends them to linked input hatches every half second.
- If the buffer for a packet has no room, whatever does not fit is **discarded** and the recipe carries on. A full output hatch never stops a recipe.
- With energy on, it also accepts EU from adjacent cables and machines and sends it to linked input hatches (section 5).

### 2.3 Input hatch (receiver)

- Stores received packets until a recipe uses them. Each packet name holds up to 10,000,000.
- Holds `data resonance` as a level (section 4).
- With energy on, it pushes received EU into adjacent cables and machines (section 5).

### 2.4 Hatch screen

| Element | Shown on | Meaning |
|---|---|---|
| Energy bar | Both, when energy is on | Stored and maximum EU |
| Resonance bar | Input hatch | Current level and capacity |
| Stored packets | Both | Scrolling list of packet names and amounts |
| Linked hatches | Output hatch | The linked input hatches with their coordinates and an `x` to remove one |
| Beam colour | Output hatch | Switches between the forward and return colours of the bridge |
| Clear all links | Output hatch | Removes every link |
| Empty buffer | Both | Voids every stored packet and the energy buffer. Asks for a second click |

### 2.5 Placement

Hatches work in two ways:

- **In a multiblock**, as a part, through the abilities `GTX.bridgeInput('<bridge>')` and `GTX.bridgeOutput('<bridge>')`. Recipes use them for packets.
- **Standalone**, as a plain block next to a cable or machine. This is how energy transfer is used.

The energy part of a hatch is not a recipe resource. A multiblock's recipes cannot draw EU from a bridge hatch, and a hatch in a multiblock wall has only casings as neighbours, so it has nothing to push energy into. Put energy hatches in the world.

---

## 3. Linking

Use the Link Tool.

1. Click the **output** hatch to select it.
2. Right click an **input** hatch of the **same bridge**.

Rules:

- A link always runs from an output to an input. A loop needs one link in each direction.
- Up to 10 inputs per output. Packets go to them in order or round robin.
- The distance limit for the Link Tool is the larger of the bridge's `range` and, if energy is on, `energy.range`.
- Packets travel only within `range`. Energy travels only within `energy.range`.
- Both ends must be in loaded chunks for anything to move.

---

## 4. Packets and resonance

### 4.1 Packet format

```
'<bridge> <kind> <name> [amount]'
```

| Part | Meaning |
|---|---|
| `<bridge>` | The bridge id, for example `einstein_rosen` |
| `<kind>` | `item`, `fluid`, `tag` or `data` |
| `<name>` | An item id, fluid id, tag id or any name for `data` |
| `[amount]` | A whole number. Optional, counts as 1 when left out |

| Kind | Example | Meaning |
|---|---|---|
| `item` | `'einstein_rosen item gtceu:bose_einstein_condensate 1024'` | A count of one item id |
| `fluid` | `'einstein_rosen fluid minecraft:water 1000'` | An amount of one fluid id |
| `tag` | `'einstein_rosen tag forge:ingots/iron 8'` | As an **input**, accepts any `item` packet whose item is in the tag |
| `data` | `'einstein_rosen data resonance 200'` | A named number with no item behind it |

A packet with a missing or extra part is rejected and the recipe fails to load, with a message that shows the string.

### 4.2 Matching

Packets match on kind and name. A recipe asking for `item minecraft:cobblestone` takes exactly that. A recipe can only take a packet that has really arrived at an input hatch it can see.

The recipe search looks at the bridge channel only. Two recipes that take different packets on the same bridge are told apart by their other inputs, so give them different **programmed circuits**.

### 4.3 What a packet does not do

A packet carries no items. Sending `item minecraft:cobblestone 64` means "64 cobblestone were fed in over there". The recipe that receives it chooses what to produce. It can produce 64 cobblestone, one diamond, or nothing at all.

### 4.4 Resonance

`data resonance` is special.

| Property | Value |
|---|---|
| Held by | Input hatches |
| Capacity | `resonanceCap` times a tier multiplier. LV is 1x, MV 2x, HV 3x, EV 4x, IV 5x, LuV 6x, ZPM 7x, UV 8x |
| Decay | `resonanceDecayPerSecond` per second |
| Shown | Resonance bar on the input hatch screen |
| Used by | Recipe modifiers (below). Nothing consumes it automatically |

Example: with `resonanceCap` 1000, a UV input hatch holds 8,000. With `resonanceCap` 10000 it holds 80,000.

Modifiers go in a machine's recipe modifiers. They read the highest resonance among the machine's input hatches of that bridge, as a fraction of capacity:

| Modifier | Effect |
|---|---|
| `GTX.bridgeYield('einstein_rosen', 'resonance', 0.5)` | Multiplies the recipe's outputs by `1 + 0.5 × fraction`, so up to 1.5 at a full bar |
| `GTX.bridgeSpeed('einstein_rosen', 'resonance', 0.5)` | Multiplies the duration by `1 − 0.5 × fraction`, so down to half at a full bar |

Both leave a recipe unchanged at zero resonance. Any other `data` name works the same way.

**What the number means.** The third argument is the largest bonus, reached when the bar is completely full. `0.5` means up to 50% more output. The bonus grows in a straight line with the fraction of the bar that is filled, so at 40% of the bar a `0.5` modifier gives a 20% bonus.

**Packets are whole numbers, so the bonus is applied by chance.** A packet of 1 at a 20% bonus has no exact value of 1.2. The mod rounds up with a probability equal to the leftover part. A packet of 1 becomes 2 one time in five at 20%, and every time at a full bar with `0.5`. Over many cycles the average output is exactly `amount × (1 + bonus × fraction)`. A packet of 100 at the same bonus becomes 120 every time. Bigger packets show the bonus more steadily.

**Free loops.** A recipe that outputs resonance and a machine that turns resonance into extra packets can create resources from nothing. Check this when you design a loop.

---

## 5. Wireless energy

Available on bridges with `energy.enabled` set to true.

### 5.1 How it works

```
 cable / machine ──► OUTPUT hatch ══ link ══► INPUT hatch ──► cable / machine
        (EU in)       (buffer)                  (buffer)         (EU out)
```

- **Output hatch.** Accepts EU from adjacent cables and machines at up to its tier voltage. Anything above the tier voltage is refused. The EU goes into a buffer.
- **Transfer.** Every half second the output hatch moves its buffer along its links to input hatches within `energy.range`. Only what fits in the receiver is moved. A percentage set by `lossPercent` is lost in transit.
- **Input hatch.** Pushes the EU it holds into any adjacent cable or machine that accepts it, every tick, at its own tier voltage. It can output on all six sides and starts pushing once it holds at least one packet of its voltage.

If an output hatch has no links, its energy stays in the buffer.

### 5.2 Buffer

The buffer size in EU is:

- `energy.bufferSize`, if it is greater than 0, or
- otherwise voltage × amperage × `bufferTicks`, where amperage is the hatch rating, or `maxAmperage` if that is set, or 1,024 when neither is set.

**When the buffer is full the hatch accepts no more.** It reports that it cannot take another packet, so cables back up or the sending machine stops, and nothing is lost. The same holds for an input hatch that cannot pass its EU on: it fills, then the sender's transfer to it stops.

### 5.3 Tier matching

By default energy only flows between hatches of the **same tier**. A UV output hatch linked to an LV input hatch moves nothing. Without this rule a bridge would work as a free voltage transformer. Set `energy.allowTierMismatch` to true to allow it.

Packets are not affected. Hatches of different tiers still link and exchange packets.

### 5.4 Amperage

With an `amperages` list, each tier gets a hatch for each listed rating. The rating is the number of amps the hatch takes in or pushes out per tick, and it sizes the buffer. Without a list, `maxAmperage` applies to every hatch, and 0 means no cap.

A hatch pushing into a machine is limited by what the machine accepts.

### 5.5 Voltage

A hatch outputs at its tier voltage. Feeding a machine of a lower tier overvolts it. A GregTech energy hatch accepts energy only through its front face, so the bridge hatch has to touch that face. A cable touching the bridge hatch works from any side and is the flexible layout.

### 5.6 Limits

- Both ends must be in loaded chunks.
- An input hatch is an end point. It does not pass energy to another bridge hatch.
- Energy is not a recipe capability. Recipes cannot read or produce it.

---

## 6. The definition file

Read at startup from `kubejs/config/gtextras_bridges.json`, or from `config/gtextras-bridges.json` if that does not exist. Changing it needs a full restart.

If neither file exists, the mod writes the default to `config/gtextras-bridges.json`. It never overwrites an existing file and never adds fields to one.

### 6.1 Default file

```json
{
  "einstein_rosen": {
    "displayName": "Einstein-Rosen Bridge",
    "color": "000000",
    "returnColor": "FFFFFF",
    "resonanceCap": 10000,
    "resonanceDecayPerSecond": 1,
    "range": 24,
    "tiers": ["uv"],
    "hatchItem": "#gtceu:circuits/uxv",
    "energy": { "enabled": false }
  },
  "wireless_energy": {
    "displayName": "Wireless Energy",
    "color": "FFC94A",
    "returnColor": "3A2A08",
    "range": 64,
    "tiers": ["uv"],
    "hatchItem": "#gtceu:circuits/opv",
    "energy": {
      "enabled": true,
      "range": 64,
      "lossPercent": 0,
      "maxAmperage": 0,
      "bufferTicks": 20,
      "bufferSize": 0,
      "allowTierMismatch": false,
      "amperages": [1, 2, 4, 8, 16]
    }
  }
}
```

### 6.2 Bridge fields

| Field | Default | Meaning |
|---|---|---|
| `displayName` | the id, prettified | Name shown on hatches and in tooltips |
| `color` | `6FD3FF` | Forward beam colour, hex RGB |
| `returnColor` | same as `color` | Return beam colour |
| `resonanceCap` | 1000 | Resonance capacity of an LV input hatch |
| `resonanceDecayPerSecond` | 5 | Resonance lost per second |
| `range` | 24 | Link range in blocks for packets |
| `tiers` | LV to UV | Tiers to register, such as `["mv", "hv"]`. Names are not case sensitive. An empty or missing list registers all eight. Unknown names are logged and skipped |
| `hatchItem` | `minecraft:ender_eye` | Item above the hull in the hatch crafting recipes. An item id, or an item tag with a leading `#`. Not an item or tag falls back to the Eye of Ender with a warning. A tag with no items makes the recipe uncraftable and is not warned about |

### 6.3 Energy fields

All of these sit inside the `energy` object.

| Field | Default | Meaning |
|---|---|---|
| `enabled` | true | Turns energy transfer on or off for this bridge |
| `range` | 64 | Link range in blocks for energy |
| `lossPercent` | 0 | Percentage lost in transit, 0 to 99 |
| `maxAmperage` | 0 | Amps per hatch per tick when no rating list is used. 0 is uncapped |
| `bufferTicks` | 20 | Ticks of throughput the automatic buffer holds |
| `bufferSize` | 0 | Exact buffer size in EU. 0 means automatic from `bufferTicks` |
| `allowTierMismatch` | false | Allows energy between hatches of different tiers |
| `amperages` | none | Ratings to register hatches for. Valid values are 1, 2, 4, 8, 16. Others are logged and skipped. With energy off the list is ignored |

### 6.4 Notes

- A field in the wrong place is ignored without a warning. `hatchItem` and `tiers` belong directly in the bridge object, not inside another block.
- A bridge removed from the file loses its hatches on the next start. Placed hatches of that bridge disappear with it.

---

## 7. Crafting recipes

Every hatch has a crafting recipe in the mod's data. The shipped ones:

- **Plain and 1A hatches.** A shaped recipe with a machine hull, two circuits and two plates of the tier, and the bridge's `hatchItem` in the plate row next to the hull.
- **Higher ratings (2A to 16A).** A shapeless recipe: two hatches of half the rating, plus the `hatchItem`. A 16A hatch needs 16 1A hatches and 15 of the `hatchItem`.

The recipes use the ingredient type `gtextras:bridge_item`, which reads the bridge's `hatchItem` when recipes load. Every recipe also has a condition that the hatch exists, so hatches removed by `tiers` or `amperages` do not cause recipe errors.

---

## 8. KubeJS

```js
const GTX = Java.loadClass('com.gtextras.api.GTX')
```

| Member | Use |
|---|---|
| `GTX.BRIDGE` | The recipe capability. `.input(GTX.BRIDGE, '<packet>')` and `.output(GTX.BRIDGE, '<packet>')` |
| `GTX.bridgeInput('<bridge>')` | Multiblock ability for the input hatches of a bridge |
| `GTX.bridgeOutput('<bridge>')` | Multiblock ability for the output hatches |
| `GTX.bridgeYield(bridge, data, maxBonus)` | Recipe modifier, section 4.4 |
| `GTX.bridgeSpeed(bridge, data, maxBonus)` | Recipe modifier, section 4.4 |

The method names are `.input` and `.output`. There is no `.inputBridge` or `.outputBridge`.

Declare `const GTX` once per script scope. If another server script at the top level already declares it, put yours inside the event handler or remove it, or the script fails with "redeclaration of const".

### 8.1 Pattern example

```js
.pattern(definition => FactoryBlockPattern.start()
    ...
    .where('H', Predicates.abilities(GTX.bridgeOutput('einstein_rosen')).setMinGlobalLimited(1).setMaxGlobalLimited(1))
    .where('I', Predicates.abilities(GTX.bridgeInput('einstein_rosen')).setMaxGlobalLimited(1))
    ...)
```

### 8.2 Machine with resonance

```js
allthemods.create('black_hole', 'multiblock')
    .rotationState(RotationState.NON_Y_AXIS)
    .recipeTypes('black_hole')
    .recipeModifiers([GTX.bridgeYield('einstein_rosen', 'resonance', 0.5)])
```

---

## 9. Worked example: a loop between two machines

Black hole eats items and sends a packet. White hole receives the packet and produces the product. The white hole also sends resonance back, which boosts the black hole.

```js
ServerEvents.recipes(allthemods => {
    const GTX = Java.loadClass('com.gtextras.api.GTX');
    const [ULV, LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV, UEV, UIV, UXV, OpV, MAX] = GTValues.VA

    const feedDuration = 100
    const releaseDuration = 100
    const releaseEut = UV

    allthemods.recipes.gtceu.black_hole('example:black_hole_feed')
        .itemInputs('1024x gtceu:bose_einstein_condensate')
        .output(GTX.BRIDGE, 'einstein_rosen item gtceu:bose_einstein_condensate 1024')
        .circuit(2)
        .duration(feedDuration)

    allthemods.recipes.gtceu.white_hole('example:white_hole_release')
        .input(GTX.BRIDGE, 'einstein_rosen item gtceu:bose_einstein_condensate 1024')
        .itemOutputs('1x gtceu:tesseract')
        .output(GTX.BRIDGE, 'einstein_rosen data resonance 200')
        .circuit(2)
        .duration(releaseDuration)
        .EUt(releaseEut)
})
```

Setup:

1. Black hole: a bridge **output** hatch, an item input bus, a maintenance hatch.
2. White hole: a bridge **input** hatch, an item output bus, a UV energy input hatch, a maintenance hatch.
3. Link the black hole's output hatch to the white hole's input hatch.
4. For resonance: a second pair going back. White hole output hatch linked to a black hole input hatch.
5. Put the matching programmed circuit in each machine.

---

## 10. Worked example: wireless power

1. Config: `wireless_energy` with energy on, `"tiers": ["uv"]`.
2. Craft a UV Wireless Energy **output** hatch and a UV **input** hatch.
3. Place the output hatch touching a UV cable or generator.
4. Place the input hatch within 64 blocks, touching a UV cable or a machine's energy input face.
5. Link them with the Link Tool.

The input hatch's screen shows its energy bar filling and emptying. Both ends must be in loaded chunks.

---

## 11. Making a new bridge

1. Add the bridge to the definition file with its own `color` and, if you want, `tiers`, `hatchItem` and `energy`.
2. Copy the `wireless_energy` asset files for each tier you register, and rename them to your bridge. The files are:
   - `assets/gtextras/blockstates/<bridge>_<input|output>_hatch_<tier>.json`
   - `assets/gtextras/models/block/machine/<bridge>_<input|output>_hatch_<tier>.json`
   - `assets/gtextras/models/item/<bridge>_<input|output>_hatch_<tier>.json`
   - `assets/gtextras/models/block/machine/part/<bridge>_<input|output>_hatch.json`
   - `assets/gtextras/textures/block/overlay/machine/overlay_<bridge>_<input|output>.png` and `_emissive.png`
3. For rated hatches, add the same files with `_<n>a` after the tier.
4. Add a crafting recipe for each hatch. For the centre ingredient use `{"type": "gtextras:bridge_item", "bridge": "<bridge>"}`.
5. Add the hatch names to a lang file under `block.gtextras.<hatch id>`.
6. Recolour the overlay PNGs to match the bridge colour. The colour of the overlay is part of the texture.

These files can live in a resource pack or in the pack's `kubejs/assets/gtextras` folder.

---

## 12. Troubleshooting

| Symptom | Cause and fix |
|---|---|
| Recipe never starts | The input hatch has no packet, or two recipes take the same packet. Link the hatches, check the stored list on the screen, and give recipes different circuits |
| Recipe stops with "Insufficient Outputs" | Fixed in 0.21.0. Output hatches discard what does not fit |
| `Cannot find function outputBridge` | The method is `.output(GTX.BRIDGE, ...)`, not `.outputBridge` |
| `redeclaration of const GTX` | Another script declares it at the top level. Move yours inside the event |
| A packet "is not in that format" | Wrong number of parts. It is `<bridge> <kind> <name> [amount]` |
| Resonance stays at 0 | No link from a white hole output hatch to a black hole input hatch, out of range, or decay beats the gain |
| Resonance does not grow items | The black hole's bonus reads only its own bridge input hatches. Check the hatch is built in and linked, that the modifier is in the registration, and that the resonance has had time to build. With small packets the bonus is applied by chance, so watch a few cycles |
| Energy does not flow | Different tiers (allow it in the config), an unlinked output hatch, out of range, an unloaded chunk, a full receiver, or nothing next to the input hatch that accepts energy |
| Machine next to an input hatch exploded | Its tier is lower than the hatch. Match tiers |
| Hatch has the wrong ingredient in JEI | `hatchItem` is misplaced in the config or not an item or tag |
| Hatches missing | The tier is not in `tiers`, or the bridge is not in the config file you are editing |
| Config changes ignored | The file you edited is not the one read. `kubejs/config/gtextras_bridges.json` wins over `config/gtextras-bridges.json` |
| Recipe cards show no bridge lines | Update to 0.21.0. The lines were added to the card text system |

---

## 13. Recipe viewer

On JEI and EMI cards, bridge packets are listed below the usual lines:

```
bridge in:
gtceu:bose_einstein_condensate

bridge out:
data resonance
```

The card grows to fit the lines. Item, fluid and tag packets show the name. `data` packets show `data <name>`. Names over 30 characters are cut off.

---

## 14. Known limits

- Bridges, packets, resonance, wireless energy, amperage ratings and tag recipes are new and have not all been tested in game by the author.
- Hatch assets for new tiers or ratings must be created by hand (section 11). The shipped assets cover both built-in bridges.
- Energy is not a recipe resource. Multiblock recipes cannot draw from bridge hatches.
- The yield modifier rounds to whole packets.
