package com.ixnah.mc.paperarc.bridge.api;

import com.ixnah.mc.paperarc.bridge.ItemEntityBridge;
import com.ixnah.mc.paperarc.bridge.LivingEntityFieldsBridge;
import net.kyori.adventure.util.TriState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;

/**
 * paper 抽出来的那批"能力小接口"（{@code RangedEntity}/{@code Shearable}/…）的实现体。
 *
 * <p>这些接口 paper 是用 {@code CraftRangedEntity}/{@code PaperShearable} 这种
 * "带 default 方法体的中间接口"混进 Craft 类的；我们没法给运行时 Craft 类加父接口，
 * 改为在对应的 {@code *IfaceMixin} 上写 default 方法体、转调这里的静态实现。
 *
 * <p>方法体放这里而不是直接写在 mixin 里，是因为 mixin 内的辅助方法合并后描述符仍指向
 * mixin 类（见 {@code docs/mixin-conventions.md}）；顶层 bridge 类没有这个问题。
 * 参数一律收成 {@code Object}，由调用方（接口 default 方法里的 {@code this}）保证类型。
 */
public final class PaperarcEntityTraits {

    private PaperarcEntityTraits() {
    }

    private static net.minecraft.world.entity.Entity handle(Object self) {
        return ((CraftEntity) self).getHandle();
    }

    // ---------------------------------------------- com.destroystokyo.paper.entity.RangedEntity

    /** paper {@code CraftRangedEntity#rangedAttack}。 */
    public static void rangedAttack(Object self, org.bukkit.entity.LivingEntity target, float charge) {
        ((RangedAttackMob) handle(self)).performRangedAttack(((CraftLivingEntity) target).getHandle(), charge);
    }

    /** paper {@code CraftRangedEntity#setChargingAttack}：vanilla 用 aggressive 位表示"举手"。 */
    public static void setChargingAttack(Object self, boolean raiseHands) {
        ((Mob) handle(self)).setAggressive(raiseHands);
    }

    // ---------------------------------------------- io.papermc.paper.entity.Frictional

    /**
     * paper {@code CraftLivingEntity/CraftItem#getFrictionState}。
     *
     * <p>Paper 在 {@code LivingEntity} 与 {@code ItemEntity} 上各加了一个
     * {@code frictionState} 字段，这里由 {@code entity.*FieldsMixin} 注入、经 duck
     * 接口读写；{@code org.bukkit.entity.Item} 与 {@code LivingEntity} 是两套宿主，
     * 用 instanceof 分流。
     */
    public static TriState getFrictionState(Object self) {
        net.minecraft.world.entity.Entity handle = handle(self);
        return handle instanceof net.minecraft.world.entity.LivingEntity
                ? ((LivingEntityFieldsBridge) handle).paper$getFrictionState()
                : ((ItemEntityBridge) handle).paper$getFrictionState();
    }

    public static void setFrictionState(Object self, TriState state) {
        net.minecraft.world.entity.Entity handle = handle(self);
        if (handle instanceof net.minecraft.world.entity.LivingEntity) {
            ((LivingEntityFieldsBridge) handle).paper$setFrictionState(state);
        } else {
            ((ItemEntityBridge) handle).paper$setFrictionState(state);
        }
    }
}
