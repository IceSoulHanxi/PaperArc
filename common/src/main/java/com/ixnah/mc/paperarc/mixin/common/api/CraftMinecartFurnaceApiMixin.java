package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.vehicle.MinecartFurnace;
import org.bukkit.craftbukkit.v.entity.CraftMinecartFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Expose-furnace-minecart-push-values API to {@link CraftMinecartFurnace}.
 *
 * <p>{@code MinecartFurnace.xPush}/{@code zPush} are public fields; the methods are
 * one-line accessors mirroring the Paper patch.</p>
 */
@Mixin(CraftMinecartFurnace.class)
public abstract class CraftMinecartFurnaceApiMixin {

    @Shadow
    public abstract MinecartFurnace getHandle();

    @Unique
    public double getPushX() {
        return getHandle().xPush;
    }

    @Unique
    public double getPushZ() {
        return getHandle().zPush;
    }

    @Unique
    public void setPushX(double xPush) {
        getHandle().xPush = xPush;
    }

    @Unique
    public void setPushZ(double zPush) {
        getHandle().zPush = zPush;
    }
}
