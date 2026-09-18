package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.SculkSensorRangeBridge;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftSculkSensor;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Configurable-sculk-sensor-listener-range.patch additions on
 * {@link CraftSculkSensor}: {@code get/setListenerRange()}.
 *
 * <p>范围覆盖值存在 NMS 侧（{@link SculkSensorRangeBridge}，落在快照方块实体的
 * {@code VibrationUser} 上），不是 Craft 字段 —— {@code getState()} 每次返回新快照，
 * Craft 侧字段活不过一次取状态，而且不会影响真实的振动监听半径也不持久化
 * （checklist §1.10 an，A6/X-1）。{@code update()} 经 NBT 回写到真实方块实体。</p>
 */
@Mixin(CraftSculkSensor.class)
public abstract class CraftSculkSensorApiMixin {

    @Unique
    private SculkSensorBlockEntity paperarc$snapshot() {
        return (SculkSensorBlockEntity) ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
    }

    // Paper start - Configurable sculk sensor listener range
    @Unique
    public int getListenerRange() {
        return this.paperarc$snapshot().getListener().getListenerRadius();
    }

    @Unique
    public void setListenerRange(int range) {
        Preconditions.checkArgument(range > 0, "Vibration listener range must be greater than 0");
        ((SculkSensorRangeBridge) this.paperarc$snapshot().getVibrationUser())
                .paperarc$setRangeOverride(range);
    }
    // Paper end - Configurable sculk sensor listener range
}
