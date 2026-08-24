package com.ixnah.mc.paperarc.mixin.fabric.block;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

/**
 * Port of Paper's AnvilDamageEvent
 * (AnvilDamageEvent.patch).
 *
 * The anvil damage roll lives in AnvilMenu's private static damage helper
 * (runtime intermediary name method_24922, signature (Player, Level, BlockPos)
 * — the compile-jar shape lambda$onTake$2(Player, float, Level, BlockPos)
 * returning BlockState does NOT exist at runtime), called from onTake. Because
 * that helper is static it cannot see the menu instance, so we stash the menu
 * in a ThreadLocal at onTake HEAD and resolve the Bukkit InventoryView via
 * reflection (CraftBukkit's getBukkitView() exists at runtime under Arclight
 * but not against the mojmap compile jar).
 *
 * We wrap the INVOKE of AnvilBlock#damage inside the helper:
 *  - cancelled  -> return the input state unchanged, so the vanilla
 *                 {@code damaged == null / != state} logic leaves the anvil
 *                 visually intact (Paper returns early from the method;
 *                 letting the rest of onTake/createResult run is a minor
 *                 timing deviation).
 *  - BROKEN     -> return null so vanilla removes the block.
 *  - otherwise  -> map DamageState material back to a BlockState, preserving
 *                 the original FACING property.
 *
 * No conflict with Arclight's AnvilMenuMixin: it injects into createResult
 * (ResultContainer.setItem / broadcastChanges INVOKE) and mayPickup only.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuAnvilDamagedMixin {

    @Unique
    private static final ThreadLocal<AnvilMenu> PAPERARC$MENU = new ThreadLocal<>();

    @Unique
    private static volatile Method PAPERARC$GET_VIEW;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void paperarc$stashMenu(Player player, ItemStack stack, CallbackInfo ci) {
        PAPERARC$MENU.set((AnvilMenu) (Object) this);
    }

    @Inject(method = "onTake", at = @At("RETURN"))
    private void paperarc$clearMenu(Player player, ItemStack stack, CallbackInfo ci) {
        PAPERARC$MENU.remove();
    }

    @WrapOperation(method = "method_24922(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/AnvilBlock;damage(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState paperarc$anvilDamaged(BlockState state, Operation<BlockState> original,
                                                    @Local(argsOnly = true) Player player,
                                                    @Local(argsOnly = true) Level level,
                                                    @Local(argsOnly = true) BlockPos pos) {
        BlockState damaged = original.call(state);
        AnvilMenu menu = PAPERARC$MENU.get();
        InventoryView view = paperarc$bukkitView(menu);
        if (view == null) {
            return damaged;
        }

        AnvilDamagedEvent event = new AnvilDamagedEvent(view,
                damaged != null ? CraftBlockData.fromData(damaged) : null);
        if (!event.callEvent()) {
            // Cancelled: leave the anvil untouched by handing back the current state.
            return state;
        }
        if (event.getDamageState() == AnvilDamagedEvent.DamageState.BROKEN) {
            return null;
        }
        BlockState adjusted = ((CraftBlockData) event.getDamageState().getMaterial().createBlockData()).getState();
        return adjusted.setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING));
    }

    @Unique
    private static InventoryView paperarc$bukkitView(AnvilMenu menu) {
        if (menu == null) {
            return null;
        }
        try {
            Method m = PAPERARC$GET_VIEW;
            if (m == null) {
                m = menu.getClass().getMethod("getBukkitView");
                m.setAccessible(true);
                PAPERARC$GET_VIEW = m;
            }
            return (InventoryView) m.invoke(menu);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
