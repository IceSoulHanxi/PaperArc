package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.StonecutterInventory;
import org.bukkit.inventory.StonecuttingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Port of Paper's Add-PlayerStonecutterRecipeSelectEvent patch.
 *
 * Cancellable HEAD inject on {@code StonecutterMenu#clickMenuButton}: fires
 * {@link PlayerStonecutterRecipeSelectEvent} before the selection is applied,
 * supports plugin-side recipe swap by remapping the chosen id through
 * {@link RecipeHolder#id()}, resends menu data and stores the final index, then
 * runs {@code setupResultSlot()} — replicating Paper's replacement body.
 * Cancelled events return {@code false} after a resend (as Paper).
 *
 * The bukkit view is obtained reflectively via {@code getBukkitView()} (added
 * at runtime by Arclight; same approach as AnvilMenuAnvilDamagedMixin), and the
 * bukkit recipe via reflective {@code toBukkitRecipe()} (provided by Arclight's
 * crafting mixins). If either is unavailable, or the top inventory is not a
 * {@link StonecutterInventory}, the handler bails out and vanilla behaviour
 * proceeds unchanged. Paper's DataSlot.shared constructor change is not ported;
 * the explicit sendAllDataToRemote covers client sync instead.
 */
@Mixin(StonecutterMenu.class)
public abstract class StonecutterMenuRecipeSelectMixin {

    @Shadow
    @Final
    private DataSlot selectedRecipeIndex;

    @Shadow
    public abstract List<StonecutterRecipe> getRecipes();

    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    private void paperarc$onRecipeSelect(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!((StonecutterMenuInvoker) (Object) this).paperarc$isValidRecipeIndex(id)) {
            return; // invalid id: vanilla ignores it too
        }
        List<StonecutterRecipe> recipes = this.getRecipes();
        StonecutterRecipe holder = recipes.get(id);

        InventoryView view = paperarc$getBukkitView((AbstractContainerMenu) (Object) this);
        if (!(view != null && view.getTopInventory() instanceof StonecutterInventory topInv)) {
            return;
        }
        StonecuttingRecipe bukkitRecipe = paperarc$toBukkit(holder);
        if (bukkitRecipe == null) {
            return;
        }

        PlayerStonecutterRecipeSelectEvent event = new PlayerStonecutterRecipeSelectEvent(
            PaperArcBridge.bukkitPlayer(player),
            topInv,
            bukkitRecipe
        );
        if (!event.callEvent()) {
            player.containerMenu.sendAllDataToRemote();
            cir.setReturnValue(false);
            return;
        }

        ResourceLocation key = CraftNamespacedKey.toMinecraft(event.getStonecuttingRecipe().getKey());
        int recipeIndex = id;
        if (!recipes.get(recipeIndex).getId().equals(key)) { // recipe did NOT stay the same
            for (int i = 0; i < recipes.size(); i++) {
                if (recipes.get(i).getId().equals(key)) {
                    recipeIndex = i;
                    break;
                }
            }
        }

        player.containerMenu.sendAllDataToRemote();
        this.selectedRecipeIndex.set(recipeIndex); // set new index so listeners can read it
        ((StonecutterMenuInvoker) (Object) this).paperarc$invokeSetupResultSlot();
        cir.setReturnValue(true);
    }

    @Unique
    private static InventoryView paperarc$getBukkitView(AbstractContainerMenu menu) {
        try {
            return (InventoryView) menu.getClass().getMethod("getBukkitView").invoke(menu);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Unique
    private static StonecuttingRecipe paperarc$toBukkit(StonecutterRecipe holder) {
        try {
            return (StonecuttingRecipe) holder.getClass().getMethod("toBukkitRecipe").invoke(holder);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
