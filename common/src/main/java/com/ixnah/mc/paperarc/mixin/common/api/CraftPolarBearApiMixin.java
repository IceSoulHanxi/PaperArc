package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.PolarBear;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPolarBear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's PolarBear standing API; direct delegation to the public vanilla
 * NMS methods {@code isStanding()}/{@code setStanding(boolean)}.
 */
@Mixin(CraftPolarBear.class)
public abstract class CraftPolarBearApiMixin {

    @Shadow
    public abstract PolarBear getHandle();

    @Unique
    public boolean isStanding() {
        return getHandle().isStanding();
    }

    @Unique
    public void setStanding(boolean standing) {
        getHandle().setStanding(standing);
    }
}
