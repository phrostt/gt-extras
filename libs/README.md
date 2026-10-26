# libs

Copy these jars from your pack's `mods` folder into this folder before building.
They are compile-time only and are not committed (see `.gitignore`).

Needed:

- `gtceu-1.20.1-7.5.3.jar`
- `ldlib-forge-1.20.1-1.0.40.b.jar` (GTCEu bundles it: open the GTCEu jar, `META-INF/jarjar/`, or take it from your mods folder)
- `Registrate-MC1.20-1.3.11.jar` (also inside `META-INF/jarjar/` of the GTCEu jar)
- `kubejs-forge-2001.6.5-build.26.jar` (or whatever KubeJS 2001.6.x is in the pack)
- `rhino-forge-*.jar` (KubeJS needs it)
- `Botania-1.20.1-456-FORGE.jar`
- `ars_nouveau-1.20.1-4.12.7-all.jar`
- `create-1.20.1-6.0.8.jar`
- `BloodMagic-1.18-1.20.1-3.3.8-50.jar` (needed for the Will hatches)

Only the classes the hatch code touches must resolve, so extra jars do no harm.
