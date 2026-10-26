ServerEvents.recipes(event => {
    const GTX = Java.loadClass('com.gtextras.api.GTX');
    const [ ULV, LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV, UEV, UIV, UXV, OpV, MAX ] = GTValues.VA;

    const names = ['raw', 'corrosive', 'destructive', 'vengeful', 'steadfast'];
    const caps = [GTX.WILL_RAW, GTX.WILL_CORROSIVE, GTX.WILL_DESTRUCTIVE, GTX.WILL_VENGEFUL, GTX.WILL_STEADFAST];

    for (let i = 0; i < 5; i++) {
        let base = 11 + i * 4;

        event.recipes.gtceu.extras_test('gtextras:test_will_' + names[i] + '_in')
            .circuit(base)
            .input(caps[i], 144)
            .duration(40)
            .EUt(LV);

        event.recipes.gtceu.extras_test('gtextras:test_will_' + names[i] + '_out')
            .circuit(base + 1)
            .output(caps[i], 144)
            .duration(40)
            .EUt(LV);

        event.recipes.gtceu.extras_test('gtextras:test_will_' + names[i] + '_in_tick')
            .circuit(base + 2)
            .input(caps[i], 4)
            .perTick(true)
            .duration(100)
            .EUt(LV);

        event.recipes.gtceu.extras_test('gtextras:test_will_' + names[i] + '_out_tick')
            .circuit(base + 3)
            .output(caps[i], 4)
            .perTick(true)
            .duration(100)
            .EUt(LV);
    }

    event.recipes.gtceu.extras_test('gtextras:test_will_all_in')
        .circuit(31)
        .input(GTX.WILL_RAW, 144)
        .input(GTX.WILL_CORROSIVE, 144)
        .input(GTX.WILL_DESTRUCTIVE, 144)
        .input(GTX.WILL_VENGEFUL, 144)
        .input(GTX.WILL_STEADFAST, 144)
        .duration(40)
        .EUt(LV);

    event.recipes.gtceu.extras_test('gtextras:test_will_all_out')
        .circuit(32)
        .output(GTX.WILL_RAW, 144)
        .output(GTX.WILL_CORROSIVE, 144)
        .output(GTX.WILL_DESTRUCTIVE, 144)
        .output(GTX.WILL_VENGEFUL, 144)
        .output(GTX.WILL_STEADFAST, 144)
        .duration(40)
        .EUt(LV);

    event.recipes.gtceu.extras_test('gtextras:test_will_transfer_small')
        .circuit(5)
        .input(GTX.WILL_RAW, 5)
        .output(GTX.WILL_RAW, 5)
        .duration(40)
        .EUt(LV);
});
