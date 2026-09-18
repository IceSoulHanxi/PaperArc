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
 * <p>覆盖值存在 NMS {@code SculkSensorBlockEntity.rangeOverride} 上
 * （{@code SculkSensorBlockEntityFieldsMixin}，NBT 键 {@code Paper.ListenerRange}），
 * 因为方块状态是快照、每次 {@code getState()} 都是新的 Craft 包装对象，
 * 挂在包装对象上的状态取一次就丢（checklist §1.10 an）。未设置时回落到
 * 快照的 vibration user 的 vanilla 半径。</p>
 */
@Mixin(CraftSculkSensor.class)
public abstract class CraftSculkSensorApiMixin {

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
        Integer override = ((com.ixnah.mc.paperarc.bridge.SculkSensorRangeBridge) this.getSnapshot())
                .paperarc$getRangeOverride();
        if (override != null) {
            return override;
        }
        return this.getSnapshot().getListener().getListenerRadius();
    }

    @Unique
    public void setListenerRange(int range) {
        Preconditions.checkArgument(range > 0, "Vibration listener range must be greater than 0");
        ((com.ixnah.mc.paperarc.bridge.SculkSensorRangeBridge) this.getSnapshot()).paperarc$setRangeOverride(range);
    }
    // Paper end - Configurable sculk sensor listener range

}
