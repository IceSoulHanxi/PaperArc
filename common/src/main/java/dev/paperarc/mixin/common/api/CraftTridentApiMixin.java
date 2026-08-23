package dev.paperarc.mixin.common.api;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.bukkit.craftbukkit.v.entity.CraftTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;

/**
 * Adds Paper's Missing-Entity-API trident methods
 * (patches/server/Missing-Entity-API.patch -> CraftTrident/ThrownTrident).
 *
 * Paper's NMS-side patch adds getLoyalty/setLoyalty/setFoil that are pure
 * synched-entity-data reads/writes on ID_LOYALTY / ID_FOIL, plus direct access
 * to {@code dealtDamage}. Vanilla already owns those private members, so the
 * Craft-host mixin resolves them reflectively and mirrors the exact semantics.
 */
@Mixin(CraftTrident.class)
public abstract class CraftTridentApiMixin {

    @Shadow
    public abstract ThrownTrident getHandle();

    @Unique
    private static volatile EntityDataAccessor<Byte> PAPERARC$ID_LOYALTY;

    @Unique
    private static volatile EntityDataAccessor<Boolean> PAPERARC$ID_FOIL;

    @Unique
    private static volatile Field PAPERARC$DEALT_DAMAGE;

    @Unique
    @SuppressWarnings("unchecked")
    private static synchronized void paperarc$resolveAccessors() {
        if (PAPERARC$ID_LOYALTY != null) {
            return;
        }
        try {
            Field loyalty = ThrownTrident.class.getDeclaredField("ID_LOYALTY");
            loyalty.setAccessible(true);
            PAPERARC$ID_LOYALTY = (EntityDataAccessor<Byte>) loyalty.get(null);
            Field foil = ThrownTrident.class.getDeclaredField("ID_FOIL");
            foil.setAccessible(true);
            PAPERARC$ID_FOIL = (EntityDataAccessor<Boolean>) foil.get(null);
            Field dealtDamage = ThrownTrident.class.getDeclaredField("dealtDamage");
            dealtDamage.setAccessible(true);
            PAPERARC$DEALT_DAMAGE = dealtDamage;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS ThrownTrident data accessors not found", e);
        }
    }

    @Unique
    public int getLoyaltyLevel() {
        paperarc$resolveAccessors();
        return getHandle().getEntityData().get(PAPERARC$ID_LOYALTY);
    }

    @Unique
    public void setLoyaltyLevel(int loyaltyLevel) {
        Preconditions.checkArgument(loyaltyLevel >= 0 && loyaltyLevel <= 127, "The loyalty level has to be between 0 and 127");
        paperarc$resolveAccessors();
        getHandle().getEntityData().set(PAPERARC$ID_LOYALTY, (byte) loyaltyLevel);
    }

    @Unique
    public boolean hasGlint() {
        return getHandle().isFoil();
    }

    @Unique
    public void setGlint(boolean glint) {
        paperarc$resolveAccessors();
        getHandle().getEntityData().set(PAPERARC$ID_FOIL, glint);
    }

    @Unique
    public boolean hasDealtDamage() {
        paperarc$resolveAccessors();
        try {
            return PAPERARC$DEALT_DAMAGE.getBoolean(getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS ThrownTrident.dealtDamage not accessible", e);
        }
    }

    @Unique
    public void setHasDealtDamage(boolean hasDealtDamage) {
        paperarc$resolveAccessors();
        try {
            PAPERARC$DEALT_DAMAGE.setBoolean(getHandle(), hasDealtDamage);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to write NMS ThrownTrident.dealtDamage", e);
        }
    }
}
