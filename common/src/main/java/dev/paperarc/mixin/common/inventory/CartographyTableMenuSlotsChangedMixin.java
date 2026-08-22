package dev.paperarc.mixin.common.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ResultContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mirrors the Paper CartographyItemEvent patch part 2: the anonymous result
 * container created in the {@link CartographyTableMenu} constructor no longer
 * calls back into {@code slotsChanged}, so setting the result slot via the API
 * does not recompute it. Cancelling {@code slotsChanged} when invoked with the
 * result container reproduces exactly that removal.
 */
@Mixin(CartographyTableMenu.class)
public abstract class CartographyTableMenuSlotsChangedMixin {

    @Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true)
    private void paperarc$skipResultRecompute(Container container, CallbackInfo ci) {
        if (container instanceof ResultContainer) {
            ci.cancel();
        }
    }
}
