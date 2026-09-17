package com.ixnah.mc.paperarc.bridge;

import org.bukkit.SoundGroup;

/**
 * paper-api {@code com.destroystokyo.paper.block.BlockSoundGroup} 的最小实现，
 * 委托 CraftBukkit 的 {@link SoundGroup}（deobf classpath 里有，Paper 侧的
 * CraftBlockSoundGroup 没有）。
 *
 * <p>放在 {@code bridge} 而不是 mixin 包：合并后的 {@code CraftBlock} 字节码要直接
 * 引用它，而 Mixin 禁止目标类引用 mixin 包内的类（IllegalClassLoadError）。</p>
 */
public final class PaperarcApiBlockSoundGroup implements com.destroystokyo.paper.block.BlockSoundGroup {

    private final SoundGroup handle;

    public PaperarcApiBlockSoundGroup(SoundGroup handle) {
        this.handle = handle;
    }

    @Override
    public org.bukkit.Sound getBreakSound() {
        return this.handle.getBreakSound();
    }

    @Override
    public org.bukkit.Sound getStepSound() {
        return this.handle.getStepSound();
    }

    @Override
    public org.bukkit.Sound getPlaceSound() {
        return this.handle.getPlaceSound();
    }

    @Override
    public org.bukkit.Sound getHitSound() {
        return this.handle.getHitSound();
    }

    @Override
    public org.bukkit.Sound getFallSound() {
        return this.handle.getFallSound();
    }
}

