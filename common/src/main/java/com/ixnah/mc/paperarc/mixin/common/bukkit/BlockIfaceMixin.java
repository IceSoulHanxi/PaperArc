package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Block} (generated).
 * Adds 15 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*

 * <p>B2-6：同时补上 paper 声明的父接口（interface mixin 的父接口会随接口一起合并进目标，
 * A2-1 实测）。父接口里**有抽象方法的**必须在 Craft 实现类上落地，否则只是把
 * NoSuchMethodError 换成 AbstractMethodError。</p>
 */
@Mixin(targets = "org.bukkit.block.Block", remap = false)
public interface BlockIfaceMixin extends net.kyori.adventure.translation.Translatable {

    @Unique
    public abstract boolean isValidTool(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract org.bukkit.block.BlockState getState(boolean p0);

    @Unique
    public abstract org.bukkit.block.Biome getComputedBiome();

    @Unique
    public abstract boolean isBuildable();

    @Unique
    public abstract boolean isBurnable();

    @Unique
    public abstract boolean isReplaceable();

    @Unique
    public abstract boolean isSolid();

    @Unique
    public abstract boolean isCollidable();

    @Unique
    public abstract boolean breakNaturally(boolean p0, boolean p1);

    @Unique
    public abstract boolean breakNaturally(org.bukkit.inventory.ItemStack p0, boolean p1, boolean p2);

    @Unique
    public abstract void tick();

    @Unique
    public abstract void fluidTick();

    @Unique
    public abstract void randomTick();

    @Unique
    public abstract com.destroystokyo.paper.block.BlockSoundGroup getSoundGroup();

    @Unique
    public abstract org.bukkit.SoundGroup getBlockSoundGroup();

    @Unique
    public abstract String translationKey();
}
