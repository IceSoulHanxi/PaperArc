package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.BeaconBlockEntityBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftBeacon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 Beacon effect-range API（Configurable-beacon-effect-range）。
 *
 * <p>Paper 在 {@code BeaconBlockEntity} 上加了一个 {@code effectRange} 字段，
 * {@code -1} 表示"按信标等级算默认范围"（{@code levels * 10 + 10}）。
 * 该字段由 {@code BeaconBlockEntityFieldsMixin} 注入，这里通过
 * {@link BeaconBlockEntityBridge} 读写。</p>
 */
@Mixin(CraftBeacon.class)
public abstract class CraftBeaconApiMixin {

    @Unique
    private BeaconBlockEntity paperarc$snapshot() {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        return snapshot instanceof BeaconBlockEntity beacon ? beacon : null;
    }

    @Unique
    public double getEffectRange() {
        BeaconBlockEntity beacon = this.paperarc$snapshot();
        if (beacon == null) {
            return 0.0D;
        }
        double range = ((BeaconBlockEntityBridge) beacon).paper$getEffectRange();
        // Paper: 负值 = 未设置，回落 vanilla 的 levels * 10 + 10
        return range < 0 ? beacon.levels * 10 + 10 : range;
    }

    @Unique
    public void setEffectRange(double effectRange) {
        Preconditions.checkArgument(effectRange >= 0, "Effect range must be at least 0");
        BeaconBlockEntity beacon = this.paperarc$snapshot();
        if (beacon != null) {
            ((BeaconBlockEntityBridge) beacon).paper$setEffectRange(effectRange);
        }
    }

    @Unique
    public void resetEffectRange() {
        BeaconBlockEntity beacon = this.paperarc$snapshot();
        if (beacon != null) {
            ((BeaconBlockEntityBridge) beacon).paper$setEffectRange(-1);
        }
    }
}
