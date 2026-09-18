package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.EvokerWololoBridge;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Evoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** 注入 Paper 的 {@code Evoker.wololoTarget}（A8/Y-3 从 Craft 侧迁来）。 */
@Mixin(Evoker.class)
public abstract class EvokerFieldsMixin implements EvokerWololoBridge {

    @Unique
    public Sheep wololoTarget; // Paper

    @Override
    public Sheep paper$getWololoTarget() {
        return this.wololoTarget;
    }

    @Override
    public void paper$setWololoTarget(Sheep sheep) {
        this.wololoTarget = sheep;
    }
}
