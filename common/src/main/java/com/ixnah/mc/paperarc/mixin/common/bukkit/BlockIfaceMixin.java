package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Block} (generated, trimmed for 1.20.1).
 * Adds 14 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>A4-3 父接口差集：补上 paper 声明的父接口 {@code net.kyori.adventure.translation.Translatable}。终端方法 translationKey() 在 CraftBlockApiMixin 上。</p>
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
    public abstract void randomTick();

    @Unique
    public abstract com.destroystokyo.paper.block.BlockSoundGroup getSoundGroup();

    @Unique
    public abstract org.bukkit.SoundGroup getBlockSoundGroup();
    @Unique
    public abstract java.lang.String getTranslationKey();
    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default boolean breakNaturally(boolean triggerEffect) {
        return breakNaturally(triggerEffect, false);
    }

    @Unique
    public default boolean breakNaturally(org.bukkit.inventory.ItemStack tool, boolean triggerEffect) {
        return breakNaturally(tool, triggerEffect, false);
    }

    @Unique
    public default long getBlockKey() {
        org.bukkit.block.Block self = (org.bukkit.block.Block) this;
        return self.getX() & 0x3FFFFFFL | (self.getZ() & 0x3FFFFFFL) << 26 | (long) self.getY() << 52;
    }

    @Unique
    public default float getDestroySpeed(org.bukkit.inventory.ItemStack itemStack) {
        return getDestroySpeed(itemStack, false);
    }

    /** paper 的 Block#getDestroySpeed(ItemStack, boolean) 是抽象方法，转调块数据侧的同名实现。 */
    @Unique
    public default float getDestroySpeed(org.bukkit.inventory.ItemStack itemStack, boolean considerEnchants) {
        return ((org.bukkit.block.Block) this).getBlockData().getDestroySpeed(itemStack, considerEnchants);
    }

}
