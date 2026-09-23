package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EvokerWololoBridge;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.illager.Evoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 把 vanilla {@code Evoker} 自带的 {@code wololoTarget} 暴露给 api mixin。
 *
 * <p><b>不要新建字段</b>：1.21.1 的 vanilla 本来就有
 * {@code private Sheep wololoTarget} 与包私有的 {@code get/setWololoTarget}
 * （`javap -p` 核对；Paper 只是把它们 publicize 了一下）。
 * 第一版按"Paper 新增字段"写成 {@code @Unique}，在 Forge/NeoForge 上当场
 * {@code Discarding @Unique public field wololoTarget … because it already exists}
 * ——而 Fabric 那边因为运行时是 intermediary 名（{@code field_NNNN}）不撞名，
 * 一个加载器绿、两个加载器红。<b>判据：给 NMS 加字段前先 javap 一遍 vanilla。</b>
 *
 * <p>直接转发到 vanilla 的那对方法，wololo 目标因此是真的被施法逻辑消费的那一个。
 */
@Mixin(Evoker.class)
public abstract class EvokerFieldsMixin implements EvokerWololoBridge {

    @Shadow
    abstract Sheep getWololoTarget();

    @Shadow
    abstract void setWololoTarget(Sheep sheep);

    @Override
    public Sheep paper$getWololoTarget() {
        return this.getWololoTarget();
    }

    @Override
    public void paper$setWololoTarget(Sheep sheep) {
        this.setWololoTarget(sheep);
    }
}
