package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 {@code Particle#builder()}（A6/X-2 第三批）。 */
@Mixin(Particle.class)
public abstract class ParticleApiMixin {

    @Unique
    public com.destroystokyo.paper.ParticleBuilder builder() {
        return new com.destroystokyo.paper.ParticleBuilder((Particle) (Object) this);
    }
}
