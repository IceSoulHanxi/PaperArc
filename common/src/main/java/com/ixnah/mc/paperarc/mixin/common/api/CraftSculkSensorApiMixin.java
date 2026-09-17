package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import org.bukkit.block.data.type.SculkSensor;
import org.bukkit.craftbukkit.v.block.CraftSculkSensor;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Configurable-sculk-sensor-listener-range.patch additions on
 * {@link CraftSculkSensor}: {@code get/setListenerRange()} plus the
 * {@code get/setPhase(Phase)} accessors.
 *
 * <p>Vanilla NMS has no per-sensor range-override field (that is a
 * Paper-side addition), so the range lives in the ApiState side map keyed by
 * the Craft state instance; unset reads fall back to the vanilla listener
 * radius from the snapshot's vibration user.</p>
 */
@Mixin(CraftSculkSensor.class)
public abstract class CraftSculkSensorApiMixin {

    /** Paper 侧补充状态（原 ApiState 副表键 "paperarc:listenerRange"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Integer paperarc$listenerRange;

    @Unique
    private static final String PAPERARC_LISTENER_RANGE_KEY = "paperarc:listenerRange";

    @Unique
    private SculkSensorBlockEntity getSnapshot() {
        return (SculkSensorBlockEntity) ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
    }

    @Unique
    private org.bukkit.block.data.BlockData getBlockData() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(org.bukkit.block.data.BlockData blockData) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(blockData);
    }

    // Paper start - Configurable sculk sensor listener range
    @Unique
    public int getListenerRange() {
        Integer override = (this.paperarc$listenerRange != null ? this.paperarc$listenerRange : (null));
        if (override != null) {
            return override;
        }
        return this.getSnapshot().getListener().getListenerRadius();
    }

    @Unique
    public void setListenerRange(int range) {
        Preconditions.checkArgument(range > 0, "Vibration listener range must be greater than 0");
        this.paperarc$listenerRange = range;
    }
    // Paper end - Configurable sculk sensor listener range

    @Unique
    public SculkSensor.Phase getPhase() {
        return ((SculkSensor) this.getBlockData()).getPhase();
    }

    @Unique
    public void setPhase(SculkSensor.Phase phase) {
        SculkSensor blockData = (SculkSensor) this.getBlockData();
        blockData.setPhase(phase);
        this.setBlockData(blockData);
    }
}
