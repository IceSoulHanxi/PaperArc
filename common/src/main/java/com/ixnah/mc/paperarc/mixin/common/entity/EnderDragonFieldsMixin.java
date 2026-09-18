package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EnderDragonPodiumBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Paper 的 {@code EnderDragon.podium} 补充字段；字段名对齐 Paper，不落盘（同 Paper）。 */
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
