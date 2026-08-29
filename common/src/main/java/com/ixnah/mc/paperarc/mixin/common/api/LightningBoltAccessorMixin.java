package com.ixnah.mc.paperarc.mixin.common.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.LightningBolt;

/**
 * Exposes private LightningBolt#life / #flashes so the Craft-side mixin can
 * read/write them without string reflection (runtime is srg-mapped; the
 * refmap handles the official→srg name remap).
 * Paper ref: patches/server/More-lightning-API.patch (getLifeTicks /
 * setLifeTicks delegate to the raw field).
 */
@Mixin(LightningBolt.class)
public interface LightningBoltAccessorMixin {

    @Accessor("life")
    int paperarc$getLife();

    @Accessor("life")
    void paperarc$setLife(int life);

    @Accessor("flashes")
    int paperarc$getFlashes();

    @Accessor("flashes")
    void paperarc$setFlashes(int flashes);
}