package dev.paperarc.mixin.mojmap.block;

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
 * NeoForge twin of the fabric AnvilMenuAnvilDamagedMixin.
 *
 * The damage roll lives in the synthetic {@code lambda$onTake$2(Player, float,
 * Level, BlockPos)} (NeoForge keeps mojmap-style lambda names and adds a float
 * argument; fabric's intermediary shape is method_24922 without it). The
 * wrapped call is AnvilBlock#damage(BlockState) on both loaders.
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

    @WrapOperation(method = "lambda$onTake$2(Lnet/minecraft/world/entity/player/Player;FLnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            remap = false,
            at = @At(value = "INVOKE", remap = false,
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
