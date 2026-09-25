package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.StonecutterMenu;
import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeHolderBridge;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
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
 * bukkit recipe via Arclight's {@code RecipeHolderBridge}. If either is unavailable, or the top inventory is not a
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
    public abstract SelectableRecipe.SingleInputSet<StonecutterRecipe> getVisibleRecipes();

    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    private void paperarc$onRecipeSelect(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!((StonecutterMenuInvoker) (Object) this).paperarc$isValidRecipeIndex(id)) {
            return; // invalid id: vanilla ignores it too
        }
        // 1.21.2 起可选配方是 SelectableRecipe 列表，服务端条目的 recipe() 带着 RecipeHolder
        List<SelectableRecipe.SingleInputEntry<StonecutterRecipe>> recipes = this.getVisibleRecipes().entries();
        RecipeHolder<StonecutterRecipe> holder = recipes.get(id).recipe().recipe().orElse(null);
        if (holder == null) {
            return;
        }

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

        Identifier key = CraftNamespacedKey.toMinecraft(event.getStonecuttingRecipe().getKey());
        int recipeIndex = id;
        if (!holder.id().identifier().equals(key)) { // recipe did NOT stay the same
            for (int i = 0; i < recipes.size(); i++) {
                java.util.Optional<RecipeHolder<StonecutterRecipe>> candidate = recipes.get(i).recipe().recipe();
                if (candidate.isPresent() && candidate.get().id().identifier().equals(key)) {
                    recipeIndex = i;
                    break;
                }
            }
        }

        player.containerMenu.sendAllDataToRemote();
        this.selectedRecipeIndex.set(recipeIndex); // set new index so listeners can read it
        ((StonecutterMenuInvoker) (Object) this).paperarc$invokeSetupResultSlot(recipeIndex);
        cir.setReturnValue(true);
    }

    // B2-1：getBukkitView 是 CraftBukkit 加在 NMS 上的方法，不是 vanilla 成员、
    // 不参与 Fabric intermediary 重映射，保留反射；配方转换走 Arclight 的 RecipeHolderBridge
    //（1.21.2 起 CB 的 toBukkitRecipe 挪到了 RecipeHolder 上，配方值上已没有无参版本）。
    @Unique
    private static InventoryView paperarc$getBukkitView(AbstractContainerMenu menu) {
        try {
            return (InventoryView) menu.getClass().getMethod("getBukkitView").invoke(menu);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Unique
    private static StonecuttingRecipe paperarc$toBukkit(RecipeHolder<StonecutterRecipe> holder) {
        return ((RecipeHolderBridge) (Object) holder).bridge$toBukkitRecipe() instanceof StonecuttingRecipe recipe
                ? recipe : null;
    }
}
