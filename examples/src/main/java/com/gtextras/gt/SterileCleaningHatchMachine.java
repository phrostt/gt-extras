package com.gtextras.gt;

import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.DummyCleanroom;
import com.gregtechceu.gtceu.common.machine.multiblock.part.CleaningMaintenanceHatchPartMachine;

import java.util.List;

public class SterileCleaningHatchMachine extends CleaningMaintenanceHatchPartMachine {

    private final DummyCleanroom sterileProvider = DummyCleanroom
            .createForTypes(List.of(CleanroomType.CLEANROOM, CleanroomType.STERILE_CLEANROOM));

    public SterileCleaningHatchMachine(IMachineBlockEntity holder) {
        super(holder, CleanroomType.STERILE_CLEANROOM);
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof ICleanroomReceiver receiver) {
            receiver.setCleanroom(sterileProvider);
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        if (controller instanceof ICleanroomReceiver receiver && receiver.getCleanroom() == sterileProvider) {
            receiver.setCleanroom(null);
        }
        super.removedFromController(controller);
    }
}
