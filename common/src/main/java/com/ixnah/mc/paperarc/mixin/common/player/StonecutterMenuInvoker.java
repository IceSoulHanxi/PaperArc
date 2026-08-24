package com.ixnah.mc.paperarc.mixin.common.player;

import net.minecraft.world.inventory.StonecutterMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor for {@link StonecutterMenu}'s non-public members needed by
 * {@link StonecutterMenuRecipeSelectMixin}: {@code setupResultSlot()} is
 * package-private and {@code isValidRecipeIndex(int)} is private, neither
 * callable from a foreign-package mixin without an invoker bridge.
 */
@Mixin(StonecutterMenu.class)
public interface StonecutterMenuInvoker {

    @Invoker("setupResultSlot")
    void paperarc$invokeSetupResultSlot();

    @Invoker("isValidRecipeIndex")
    boolean paperarc$isValidRecipeIndex(int index);
}
