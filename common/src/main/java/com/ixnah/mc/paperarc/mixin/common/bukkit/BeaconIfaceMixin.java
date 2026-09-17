package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Beacon} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>A4-3 父接口差集：补上 paper 声明的父接口 {@code io.papermc.paper.block.LockableTileState}。父接口自身没有新的抽象方法（都来自 Lockable/TileState，运行时都有），只补继承关系。</p>
 */
@Mixin(targets = "org.bukkit.block.Beacon", remap = false)
public interface BeaconIfaceMixin extends io.papermc.paper.block.LockableTileState {

    @Unique
    public abstract double getEffectRange();

    @Unique
    public abstract void setEffectRange(double p0);

    @Unique
    public abstract void resetEffectRange();
}
