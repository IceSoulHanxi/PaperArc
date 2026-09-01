package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.block.CraftCampfire;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.google.common.base.Preconditions;

import com.ixnah.mc.paperarc.bridge.CampfireBlockEntityBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

/**
 * Adds Paper's "Add more Campfire API" additions to CraftCampfire.
 *
 * Paper ref: patches/server/Add-more-Campfire-API.patch.
 *
 * Paper adds a {@code public final boolean[] stopCooking} field to NMS
 * CampfireBlockEntity (persisted via an extra NBT byte array and honored inside
 * cookTick). The field is injected into the NMS class by
 * {@code CampfireBlockEntityFieldsMixin} and reached through
 * {@link com.ixnah.mc.paperarc.bridge.CampfireBlockEntityBridge}.
 *
 * {@code CraftBlockEntityState#getSnapshot()} is protected; it is reached via
 * the merged {@link CraftBlockEntityStateBridge} (provider mixin on the base
 * class) instead of a subclass @Shadow, which Mixin fails to resolve on
 * CraftCampfire.
 */
@Mixin(CraftCampfire.class)
public abstract class CraftCampfireApiMixin {

    @Unique
    private CampfireBlockEntity paperarc$snapshot() {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        return snapshot instanceof CampfireBlockEntity cbe ? cbe : null;
    }

    @Unique
    public boolean stopCooking(int index) {
        Preconditions.checkArgument(-1 < index && index < 4, "Slot index must be between 0 (incl) to 3 (incl)");
        boolean previous = this.isCookingDisabled(index);
        ((CampfireBlockEntityBridge) this.paperarc$snapshot()).paper$setStopCooking(index, true);
        return previous;
    }

    @Unique
    public boolean startCooking(int index) {
        Preconditions.checkArgument(-1 < index && index < 4, "Slot index must be between 0 (incl) to 3 (incl)");
        boolean previous = this.isCookingDisabled(index);
        ((CampfireBlockEntityBridge) this.paperarc$snapshot()).paper$setStopCooking(index, false);
        return previous;
    }

    @Unique
    public boolean isCookingDisabled(int index) {
        Preconditions.checkArgument(-1 < index && index < 4, "Slot index must be between 0 (incl) to 3 (incl)");
        return ((CampfireBlockEntityBridge) this.paperarc$snapshot()).paper$isStopCooking(index);
    }
}
