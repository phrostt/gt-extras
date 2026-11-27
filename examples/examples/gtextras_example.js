const GTX = Java.loadClass('com.gtextras.api.GTX');

ServerEvents.recipes(event => {
    event.recipes.gtceu.your_recipe_type('gregification:example_mana_source_rpm')
        .itemInputs('minecraft:iron_ingot')
        .input(GTX.MANA, 5000)
        .perTick(true)
        .input(GTX.SOURCE, 10)
        .perTick(false)
        .addCondition(GTX.minRpm(64))
        .itemOutputs('minecraft:gold_ingot')
        .duration(200)
        .EUt(GTValues.VA[GTValues.MV]);
});
