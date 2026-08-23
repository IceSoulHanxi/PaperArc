package dev.paperarc.mixin.common.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.monster.Skeleton;

/**
 * Exposes private Skeleton#inPowderSnowTime for CraftSkeletonApiMixin#inPowderedSnowTime.
 * Paper ref: patches/server/Entity-powdered-snow-API.patch.
 */
@Mixin(Skeleton.class)
public interface SkeletonAccessorMixin {

    @Accessor("inPowderSnowTime")
    int paperarc$getInPowderSnowTime();
}
