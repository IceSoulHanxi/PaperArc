package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.projectile.ThrownTrident;
import org.bukkit.craftbukkit.v.entity.CraftTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.google.common.base.Preconditions;

/**
 * Adds Paper's Missing-Entity-API trident methods
 * (patches/server/Missing-Entity-API.patch -> CraftTrident/ThrownTrident).
 *
 * Paper's NMS-side patch adds getLoyalty/setLoyalty/setFoil that are pure
 * synched-entity-data reads/writes on ID_LOYALTY / ID_FOIL, plus direct access
 * to {@code dealtDamage}. Vanilla already owns those private members;
 * {@code paperarc.accesswidener} opens them and this mixin mirrors the exact semantics.
 */
@Mixin(CraftTrident.class)
public abstract class CraftTridentApiMixin {

    @Shadow
    public abstract ThrownTrident getHandle();

    @Unique
    public int getLoyaltyLevel() {
        return getHandle().getEntityData().get(ThrownTrident.ID_LOYALTY);
    }

    @Unique
    public void setLoyaltyLevel(int loyaltyLevel) {
        Preconditions.checkArgument(loyaltyLevel >= 0 && loyaltyLevel <= 127, "The loyalty level has to be between 0 and 127");
        getHandle().getEntityData().set(ThrownTrident.ID_LOYALTY, (byte) loyaltyLevel);
    }

    @Unique
    public boolean hasGlint() {
        return getHandle().isFoil();
    }

    @Unique
    public void setGlint(boolean glint) {
        getHandle().getEntityData().set(ThrownTrident.ID_FOIL, glint);
    }

    @Unique
    public boolean hasDealtDamage() {
        return getHandle().dealtDamage;
    }

    @Unique
    public void setHasDealtDamage(boolean hasDealtDamage) {
        getHandle().dealtDamage = hasDealtDamage;
    }
}
