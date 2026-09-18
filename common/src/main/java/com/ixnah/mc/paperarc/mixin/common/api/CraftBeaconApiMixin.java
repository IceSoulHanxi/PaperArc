package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.BeaconBlockEntityBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftBeacon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code Beacon#getEffectRange/setEffectRange/resetEffectRange}
 * （A5-3，pairing 基线 NO_IMPL）。状态存在 NMS 侧的注入字段
 * {@code BeaconBlockEntityFieldsMixin#effectRange} 上。
 *
 * <p><b>已登记的语义降级</b>（docs/gaps.md E.7）：设置的自定义半径目前只是状态，
 * 真正施加效果时用的仍是 vanilla 的 {@code levels * 10 + 10} —— Paper 是给
 * {@code getHumansInRange}/{@code applyEffects} 两个 <b>private static</b> 方法各加了一个
 * 带 {@code BeaconBlockEntity} 参数的重载来把方块实体传下去，mixin 加不了形参。</p>
 */
@Mixin(CraftBeacon.class)
public abstract class CraftBeaconApiMixin {

    @Unique
    private BeaconBlockEntity paperarc$beacon() {
        return (BeaconBlockEntity) ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
    }

    @Unique
    public double getEffectRange() {
        BeaconBlockEntity snapshot = this.paperarc$beacon();
        double range = ((BeaconBlockEntityBridge) snapshot).paper$getEffectRange();
        // levels 是 NMS 包私有字段，已由 AT 放开（srg f_58650_）
        return range < 0 ? snapshot.levels * 10.0D + 10.0D : range;
    }

    @Unique
    public void setEffectRange(double range) {
        ((BeaconBlockEntityBridge) this.paperarc$beacon()).paper$setEffectRange(range);
    }

    @Unique
    public void resetEffectRange() {
        ((BeaconBlockEntityBridge) this.paperarc$beacon()).paper$setEffectRange(-1.0D);
    }
}
