package com.ixnah.mc.paperarc.bridge;

import org.bukkit.SoundGroup;

/**
 * Minimal implementation of paper-api's
 * {@code com.destroystokyo.paper.block.BlockSoundGroup} delegating to
 * CraftBukkit's {@link SoundGroup} (returned by
 * {@code CraftSoundGroup#getSoundGroup(SoundType)}), which is present in the
 * deobf classpath.
 *
 * <p>Lives in {@code bridge} (not the mixin package) so that the merged
 * {@code CraftBlock} bytecode may reference it directly — Mixin forbids
 * referencing classes inside a defined mixin package.</p>
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