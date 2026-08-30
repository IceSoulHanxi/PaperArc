package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.ArmorStandBridge;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code ArmorStand} supplementary fields
 * (Add-API-methods-to-control-if-armour-stands-can-move.patch +
 * Allow-disabling-armour-stand-ticking.patch). Field names match Paper exactly
 * (no {@code paperarc$} prefix) so reflection on the NMS class is ABI-compatible
 * with Paper. The api mixins reach these fields through {@link ArmorStandBridge}.
 */
@Mixin(ArmorStand.class)
public abstract class ArmorStandFieldsMixin implements ArmorStandBridge {

    @Unique
    public boolean canMove = true; // Paper

    @Unique
    public boolean canTick = true; // Paper

    @Unique
    public boolean canTickSetByAPI = false; // Paper

    @Override
    public boolean paper$canMove() {
        return this.canMove;
    }

    @Override
    public void paper$setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    @Override
    public boolean paper$canTick() {
        return this.canTick;
    }

    @Override
    public void paper$setCanTick(boolean canTick) {
        this.canTick = canTick;
    }

    @Override
    public boolean paper$canTickSetByAPI() {
        return this.canTickSetByAPI;
    }

    @Override
    public void paper$setCanTickSetByAPI(boolean canTickSetByAPI) {
        this.canTickSetByAPI = canTickSetByAPI;
    }
}
