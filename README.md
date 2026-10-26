# GT Extras

Extra info for GregTech CEu in EMI and JEI, plus a set of hatches that connect GregTech to other mods.

## Recipe viewer

- Blood Magic meteors and ritual tabs
- Heating coil needed by a recipe
- Research required by a recipe
- Mana, Source and Life Essence a recipe uses, shown per tick or total
- Minimum RPM a recipe needs

## Hatches

Input and output hatches for mana, Source and Life Essence, in every tier from LV to UV. They connect to Botania pools, Ars Nouveau jars and fluid tanks, and they work as multiblock parts in KubeJS machines.

There is also a Rotation Reader Hatch for Create, which can require a minimum RPM, and a Rotation Coupler and Rotation Motor that put stress on a Create network or supply it.

## Configurable

Storage and transfer rate for each hatch and tier are in `config/gtextras-hatches.toml`. The fluids each hatch accepts, and how many units one mB is worth, are in `config/gtextras-fluids.toml`.

## Requirements

Minecraft 1.20.1, Forge, GregTech CEu 7.5.3 and EMI or JEI. Botania, Ars Nouveau, Blood Magic and Create are optional, and each hatch type only appears if its mod is installed.