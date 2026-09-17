package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.BlockType} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*

 * <p>B2-6：同时补上 paper 声明的父接口（interface mixin 的父接口会随接口一起合并进目标，
 * A2-1 实测）。父接口里**有抽象方法的**必须在 Craft 实现类上落地，否则只是把
 * NoSuchMethodError 换成 AbstractMethodError。</p>
 */
@Mixin(targets = "org.bukkit.block.BlockType", remap = false)
public interface BlockTypeIfaceMixin extends io.papermc.paper.world.flag.FeatureDependant, net.kyori.adventure.translation.Translatable {

    @Unique
    public abstract boolean hasCollision();

    @Unique
    public abstract String translationKey();
}
