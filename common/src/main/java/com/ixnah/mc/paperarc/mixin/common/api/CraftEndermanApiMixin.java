package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.EnderMan;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Enderman API (Missing-Entity-API + Enderman.teleportRandomly).
 *
 * Vanilla NMS lacks {@code setCreepy}/{@code setHasBeenStaredAt}; Paper implements
 * them as {@code entityData.set(DATA_*, v)} with private-static accessors widened
 * via AT (f_32473_ / f_32474_) and read directly. {@code teleportRandomly}
 * delegates to the protected vanilla {@code EnderMan#teleport()} (AT-widened
 * m_32529_()Z) and is called directly — no reflection.
 */
@Mixin(CraftEnderman.class)
public abstract class CraftEndermanApiMixin {

    @Shadow
    public abstract EnderMan getHandle();

    @Unique
    public boolean isScreaming() {
        return getHandle().isCreepy();
    }

    @Unique
    public void setScreaming(boolean screaming) {
        getHandle().getEntityData().set(EnderMan.DATA_CREEPY, screaming);
    }

    @Unique
    public boolean hasBeenStaredAt() {
        return getHandle().hasBeenStaredAt();
    }

    @Unique
    public void setHasBeenStaredAt(boolean hasBeenStaredAt) {
        getHandle().getEntityData().set(EnderMan.DATA_STARED_AT, hasBeenStaredAt);
    }

    @Unique
    public boolean teleportRandomly() {
        return getHandle().teleport();
    }
}
