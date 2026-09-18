package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.EnderDragonPodiumBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** 注入 Paper 的 {@code EnderDragon.podium}（A8/Y-3 从 Craft 侧迁来）。 */
@Mixin(EnderDragon.class)
public abstract class EnderDragonFieldsMixin implements EnderDragonPodiumBridge {

    @Unique
    public BlockPos podium; // Paper

    @Override
    public BlockPos paper$getPodium() {
        return this.podium;
    }

    @Override
    public void paper$setPodium(BlockPos podium) {
        this.podium = podium;
    }
}
