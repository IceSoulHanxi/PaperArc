package com.ixnah.mc.paperarc.mixin.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Dolphin.class)
public interface DolphinAccessor {

    @Accessor("treasurePos")
    BlockPos paperarc$getTreasurePos();

    @Accessor("treasurePos")
    void paperarc$setTreasurePos(BlockPos pos);
}
