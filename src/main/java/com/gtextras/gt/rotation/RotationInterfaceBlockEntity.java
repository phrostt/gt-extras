package com.gtextras.gt.rotation;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class RotationInterfaceBlockEntity extends GeneratingKineticBlockEntity implements IUIHolder.BlockEntityUI {

    public static final float MAX_SPEED = 256.0f;
    public static final float SU_PER_EU = 64.0f;
    public static final long MIN_EUT = 1L;
    public static final float MIN_IMPACT = 1.0f;
    private static final int INTERVAL = 10;

    private enum Mode {
        IDLE,
        GENERATE,
        LOAD
    }

    private Mode mode = Mode.IDLE;
    private long eut;
    private boolean blocked;
    private float outRpm;
    private boolean output = true;
    private String machineId = "";
    private int progress;
    private int duration;
    private float reqRpm;
    private String waitingReason = "";

    public RotationInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean isOutput() {
        return output;
    }

    public void toggleType() {
        if (level == null || level.isClientSide) return;
        Mode old = mode;
        output = !output;
        mode = Mode.IDLE;
        eut = 0L;
        outRpm = 0.0f;
        blocked = false;
        if (old == Mode.GENERATE) {
            updateGeneratedRotation();
        }
        if (old == Mode.LOAD && hasNetwork()) {
            getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
        }
        setChanged();
        notifyUpdate();
    }

    @Override
    protected void write(net.minecraft.nbt.CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putBoolean("InterfaceOutput", output);
        if (clientPacket) {
            compound.putInt("IfMode", mode.ordinal());
            compound.putLong("IfEut", eut);
            compound.putBoolean("IfBlocked", blocked);
            compound.putFloat("IfOut", outRpm);
            compound.putString("IfMachine", machineId);
            compound.putInt("IfProgress", progress);
            compound.putInt("IfDuration", duration);
            compound.putFloat("IfReq", reqRpm);
            compound.putString("IfWait", waitingReason);
        }
    }

    @Override
    protected void read(net.minecraft.nbt.CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        output = !compound.contains("InterfaceOutput") || compound.getBoolean("InterfaceOutput");
        if (clientPacket && compound.contains("IfMode")) {
            mode = Mode.values()[Math.max(0, Math.min(compound.getInt("IfMode"), Mode.values().length - 1))];
            eut = compound.getLong("IfEut");
            blocked = compound.getBoolean("IfBlocked");
            outRpm = compound.getFloat("IfOut");
            machineId = compound.getString("IfMachine");
            progress = compound.getInt("IfProgress");
            duration = compound.getInt("IfDuration");
            reqRpm = compound.getFloat("IfReq");
            waitingReason = compound.getString("IfWait");
        }
    }

    @Override
    public float getGeneratedSpeed() {
        return mode == Mode.GENERATE ? outRpm : 0.0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = mode == Mode.GENERATE ? Math.max(eut, MIN_EUT) * SU_PER_EU / Math.max(outRpm, 1.0f) : 0.0f;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float calculateStressApplied() {
        float applied = mode == Mode.LOAD ? Math.max(MIN_IMPACT, (float) eut) : 0.0f;
        this.lastStressApplied = applied;
        return applied;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = getLevel();
        if (level == null || level.isClientSide || level.getGameTime() % INTERVAL != 0) {
            return;
        }
        Mode newMode = Mode.IDLE;
        long newEut = 0L;
        float newSpeed = 0.0f;
        IRecipeLogicMachine controller = findController(level);
        String newMachine = "";
        int newProgress = 0;
        int newDuration = 0;
        float newReq = 0.0f;
        String newWait = "";
        if (controller != null && controller.getRecipeLogic().isWaiting()
                && controller.getRecipeLogic().getWaitingReason() != null) {
            newWait = controller.getRecipeLogic().getWaitingReason().getString();
        }
        if (controller != null) {
            newMachine = controller.self().getDefinition().getDescriptionId();
        }
        if (controller != null && controller.getRecipeLogic().isWorking()) {
            newProgress = controller.getRecipeLogic().getProgress();
            newDuration = controller.getRecipeLogic().getDuration();
            GTRecipe recipe = controller.getRecipeLogic().getLastRecipe();
            if (recipe != null) {
                newEut = Math.max(0L, Math.abs(RecipeHelper.getRealEUt(recipe).getTotalEU()));
                if (!output) {
                    newReq = minRpm(recipe);
                    if (needsRotation(recipe)) newMode = Mode.LOAD;
                } else {
                    newSpeed = outputRpm(recipe);
                    newReq = newSpeed;
                    if (newSpeed > 0.0f) newMode = Mode.GENERATE;
                }
            }
        }
        boolean wasBlocked = blocked;
        blocked = false;
        if (newMode == Mode.GENERATE && mode != Mode.GENERATE && (hasSource() || Math.abs(getTheoreticalSpeed()) > 0.0f)) {
            newMode = Mode.IDLE;
            newSpeed = 0.0f;
            blocked = true;
        }
        int pct = newDuration <= 0 ? 0 : newProgress * 100 / newDuration;
        int oldPct = duration <= 0 ? 0 : progress * 100 / duration;
        boolean infoChanged = blocked != wasBlocked || pct != oldPct || reqRpm != newReq
                || !machineId.equals(newMachine) || !waitingReason.equals(newWait) || duration != newDuration;
        machineId = newMachine;
        waitingReason = newWait;
        progress = newProgress;
        duration = newDuration;
        reqRpm = newReq;
        if (infoChanged) {
            notifyUpdate();
        }
        boolean generateChanged = (newMode == Mode.GENERATE) != (mode == Mode.GENERATE)
                || (newMode == Mode.GENERATE && (newEut != eut || newSpeed != outRpm));
        boolean loadChanged = (newMode == Mode.LOAD) != (mode == Mode.LOAD)
                || (newMode == Mode.LOAD && newEut != eut);
        mode = newMode;
        eut = newEut;
        outRpm = newSpeed;
        if (generateChanged) {
            updateGeneratedRotation();
        }
        if (loadChanged) {
            if (hasNetwork()) {
                getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
            }
            notifyUpdate();
        }
    }

    private static boolean needsRotation(GTRecipe recipe) {
        for (RecipeCondition<?> condition : recipe.conditions) {
            if (condition instanceof MinRpmCondition) return true;
        }
        return false;
    }

    private static float minRpm(GTRecipe recipe) {
        float best = 0.0f;
        for (RecipeCondition<?> condition : recipe.conditions) {
            if (condition instanceof MinRpmCondition min) best = Math.max(best, min.getRpm());
        }
        return best;
    }

    private static float outputRpm(GTRecipe recipe) {
        float best = 0.0f;
        for (RecipeCondition<?> condition : recipe.conditions) {
            if (condition instanceof OutputRpmCondition out) {
                best = Math.max(best, Math.min(MAX_SPEED, out.getRpm()));
            }
        }
        return best;
    }

    private IRecipeLogicMachine findController(Level level) {
        BlockPos pos = getBlockPos();
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (!(be instanceof IMachineBlockEntity holder)) continue;
            MetaMachine machine = holder.getMetaMachine();
            if (machine instanceof IMultiController controller && controller.isFormed()
                    && controller instanceof IRecipeLogicMachine logic) {
                return logic;
            }
            if (machine instanceof IMultiPart part) {
                for (IMultiController controller : part.getControllers()) {
                    if (controller.isFormed() && controller instanceof IRecipeLogicMachine logic) return logic;
                }
            }
        }
        if (!(level instanceof ServerLevel server)) return null;
        MultiblockWorldSavedData data = MultiblockWorldSavedData.getOrCreate(server);
        ChunkPos chunk = new ChunkPos(pos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Set<MultiblockState> states = data.getControllersInChunk(new ChunkPos(chunk.x + dx, chunk.z + dz));
                if (states == null) continue;
                for (MultiblockState state : states) {
                    IMultiController controller = state.getController();
                    if (controller == null || !controller.isFormed()
                            || !(controller instanceof IRecipeLogicMachine logic)) {
                        continue;
                    }
                    if (touches(state, pos)) return logic;
                }
            }
        }
        return null;
    }

    private static boolean touches(MultiblockState state, BlockPos pos) {
        if (state.isPosInCache(pos)) return true;
        for (Direction dir : Direction.values()) {
            if (state.isPosInCache(pos.relative(dir))) return true;
        }
        return false;
    }

    private static LabelWidget text(int x, int y, java.util.function.Supplier<String> value) {
        LabelWidget label = new LabelWidget(x, y, value);
        label.setColor(0xFF404040);
        return label;
    }

    private String modeText() {
        return switch (mode) {
            case GENERATE -> "Generating rotation";
            case LOAD -> "Consuming rotation";
            default -> blocked ? "Blocked" : !waitingReason.isEmpty() ? "Paused: " + shortText(waitingReason) : "Idle";
        };
    }

    private float networkFraction() {
        return capacity <= 0.0f ? 0.0f : Math.min(1.0f, stress / capacity);
    }

    private static String shortText(String text) {
        return text.length() > 28 ? text.substring(0, 27) + "." : text;
    }

    private String machineName() {
        if (machineId.isEmpty()) return "none";
        String name = net.minecraft.network.chat.Component.translatable(machineId).getString();
        if (name.equals(machineId)) {
            String raw = machineId.substring(machineId.lastIndexOf('.') + 1).replace('_', ' ');
            name = raw.isEmpty() ? machineId : Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
        }
        return name.length() > 30 ? name.substring(0, 29) + "." : name;
    }

    private float actualSu(float perRpm) {
        return perRpm * Math.abs(getSpeed());
    }

    @Override
    public ModularUI createUI(Player player) {
        int w = 200;
        int in = w - 16;
        WidgetGroup group = new WidgetGroup(0, 0, w, 168);
        group.addWidget(text(8, 6, () -> "Rotation Interface"));
        group.addWidget(new ButtonWidget(8, 18, in, 14, GuiTextures.BUTTON, click -> toggleType()));
        LabelWidget typeLabel = new LabelWidget(14, 21, () -> output
                ? "Type: OUTPUT (click to switch)" : "Type: INPUT (click to switch)");
        typeLabel.setColor(0xFFFFFFFF);
        group.addWidget(typeLabel);
        group.addWidget(text(8, 38, () -> "Status: " + modeText()));
        group.addWidget(text(8, 49, () -> "Machine: " + machineName()));
        group.addWidget(text(8, 60, () -> duration > 0
                ? String.format("Recipe: %d done, %,d EU/t", progress * 100 / duration, eut)
                : "Recipe: none running"));
        group.addWidget(text(8, 71, () -> output
                ? String.format("Output target: %.0f RPM", reqRpm)
                : String.format("Needs: %.0f RPM or more", reqRpm)));
        group.addWidget(text(8, 82, () -> String.format("Network speed: %.0f RPM", Math.abs(getSpeed()))));
        group.addWidget(text(8, 93, () -> switch (mode) {
            case GENERATE -> String.format("Providing: %,.0f su", actualSu(lastCapacityProvided));
            case LOAD -> String.format("Consuming: %,.0f su", actualSu(lastStressApplied));
            default -> blocked ? "Blocked: network has another source" : "Providing / consuming: 0 su";
        }));
        group.addWidget(text(8, 104, () -> String.format("Network: %,.0f / %,.0f su", stress, capacity)));
        group.addWidget(text(8, 118, () -> "Network stress"));
        group.addWidget(new ProgressWidget(() -> (double) networkFraction(), 8, 129, in, 12)
                .setProgressTexture(new ColorRectTexture(0xFF373737), new ColorRectTexture(0xFF55AA55)));
        LabelWidget bar = new LabelWidget(12, 132, () -> overStressed ? "OVERSTRESSED"
                : Math.round(networkFraction() * 100.0f) + " percent");
        bar.setColor(0xFFFFFFFF);
        group.addWidget(bar);
        group.addWidget(text(8, 148, () -> output ? "Output never accepts rotation." : "Input never generates rotation."));
        return new ModularUI(w, 168, this, player).background(GuiTextures.BACKGROUND).widget(group);
    }
}
