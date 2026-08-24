package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing {@link net.minecraft.world.entity.LivingEntity}
 * #setJumping(boolean) to mixins targeting subclasses (Panda, Ravager) without
 * needing a per-class @Shadow lookup of a protected superclass method.
 */
public interface LivingEntityBridge {

    void bridge$setJumping(boolean jumping);
}
