package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.phys.Vec3;
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
        Vec3 push = getHandle().push;
        return push != null ? push.x : 0.0;
    }

    @Unique
    public double getPushZ() {
        Vec3 push = getHandle().push;
        return push != null ? push.z : 0.0;
    }

    @Unique
    public void setPushX(double xPush) {
        Vec3 push = getHandle().push;
        getHandle().push = new Vec3(xPush, push != null ? push.y : 0.0, push != null ? push.z : 0.0);
    }

    @Unique
    public void setPushZ(double zPush) {
        Vec3 push = getHandle().push;
        getHandle().push = new Vec3(push != null ? push.x : 0.0, push != null ? push.y : 0.0, zPush);
    }
}
