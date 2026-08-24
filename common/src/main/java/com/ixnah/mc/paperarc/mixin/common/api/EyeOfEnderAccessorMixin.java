package com.ixnah.mc.paperarc.mixin.common.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.projectile.EyeOfEnder;

/**
 * Exposes private EyeOfEnder#life / #surviveAfterDeath so setTargetLocation(loc, false)
 * can restore them after the vanilla single-arg signalTo resets them.
 * Paper ref: patches/server/Change-EnderEye-target-without-changing-other-things.patch.
 */
@Mixin(EyeOfEnder.class)
public interface EyeOfEnderAccessorMixin {

    @Accessor("life")
    int paperarc$getLife();

    @Accessor("life")
    void paperarc$setLife(int life);

    @Accessor("surviveAfterDeath")
    boolean paperarc$getSurviveAfterDeath();

    @Accessor("surviveAfterDeath")
    void paperarc$setSurviveAfterDeath(boolean survive);
}
