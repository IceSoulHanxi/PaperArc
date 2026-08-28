package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v1_20_R1.entity.CraftSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.monster.Skeleton;

/**
 * Adds inPowderedSnowTime missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Entity-powdered-snow-API.patch.
 */
@Mixin(CraftSkeleton.class)
public abstract class CraftSkeletonApiMixin {

    @Shadow
    public abstract Skeleton getHandle();

    @Unique
    public int inPowderedSnowTime() {
        return ((SkeletonAccessorMixin) this.getHandle()).paperarc$getInPowderSnowTime();
    }
}
