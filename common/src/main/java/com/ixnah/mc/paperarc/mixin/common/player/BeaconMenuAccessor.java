package com.ixnah.mc.paperarc.mixin.common.player;

import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * BeaconMenu private field accessor for {@code access} (ContainerLevelAccess).
 * Used by the PlayerChangeBeaconEffectEvent handler to resolve the beacon block.
 * 1.20.1: {@code beaconData}/{@code paymentSlot} are not exposed here; the
 * handler re-enters vanilla {@code updateEffects} instead of replaying the body.
 */
@Mixin(BeaconMenu.class)
public interface BeaconMenuAccessor {

    @Accessor("access")
    ContainerLevelAccess paperarc$getAccess();
}
