package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.monster.Endermite;
import org.bukkit.craftbukkit.v.entity.CraftEndermite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Endermite lifetime API.
 *
 * Paper publicizes the private NMS field {@code Endermite.life} via an access
 * transformer; here it is widened via AT (f_32588_) and accessed directly —
 * no reflection.
 */
@Mixin(CraftEndermite.class)
public abstract class CraftEndermiteApiMixin {

    @Shadow
    public abstract Endermite getHandle();

    @Unique
    public int getLifetimeTicks() {
        return getHandle().life;
    }

    @Unique
    public void setLifetimeTicks(int ticks) {
        getHandle().life = ticks;
    }
}
