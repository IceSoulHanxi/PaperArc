package com.ixnah.mc.paperarc.mixin.common.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.block.CompostItemEvent;
import io.papermc.paper.event.entity.EntityCompostItemEvent;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's CompostItemEvent / EntityCompostItemEvent
 * (Add-CompostItemEvent-and-EntityCompostItemEvent.patch).
 *
 * Paper rewrites ComposterBlock#addItem to fire the event (entity variant when
 * an entity is composting) and honors willRaiseLevel; cancelled entity events
 * make addItem return null. We inject at HEAD: roll the real random once,
 * fire the event and short-circuit on cancel / !willRaiseLevel. When vanilla
 * continues (willRaiseLevel == true), the nextDouble call is wrapped to return
 * 0.0 so the raise branch is always taken — sound because r &lt; f implies f &gt; 0.
 */
@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin {

    @Inject(method = "addItem", at = @At("HEAD"), cancellable = true)
    private static void paperarc$compostItem(Entity entity, BlockState state, LevelAccessor level, BlockPos pos,
                                             ItemStack stack, CallbackInfoReturnable<BlockState> cir) {
        int i = state.getValue(ComposterBlock.LEVEL);
        float f = ComposterBlock.COMPOSTABLES.getFloat((ItemLike) stack.getItem());
        double rand = level.getRandom().nextDouble();
        boolean willRaiseLevel = !((i != 0 || f <= 0.0F) && rand >= (double) f);
        if (entity == null) {
            CompostItemEvent event = new CompostItemEvent(CraftBlock.at(level, pos),
                    CraftItemStack.asCraftMirror(stack), willRaiseLevel);
            if (!event.callEvent()) {
                cir.setReturnValue(null);
                return;
            }
            willRaiseLevel = event.willRaiseLevel();
        } else {
            EntityCompostItemEvent event = new EntityCompostItemEvent(PaperArcBridge.bukkitEntity(entity),
                    CraftBlock.at(level, pos), CraftItemStack.asCraftMirror(stack), willRaiseLevel);
            if (!event.callEvent()) { // only the entity variant is cancellable
                cir.setReturnValue(null);
                return;
            }
            willRaiseLevel = event.willRaiseLevel();
        }
        if (!willRaiseLevel) {
            cir.setReturnValue(state);
        }
    }

    @WrapOperation(method = "addItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextDouble()D"))
    private static double paperarc$fixedRoll(RandomSource instance, Operation<Double> original) {
        return 0.0D;
    }
}
