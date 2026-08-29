package com.ixnah.mc.paperarc.mixin.common.entity;

import io.papermc.paper.event.entity.EntityDyeEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's EntityDyeEvent for Wolf collar dyeing.
 * Same approach as {@link CatDyeMixin}: wrap DyeItem.getDyeColor() inside
 * mobInteract; cancellation returns the current collar color so the vanilla
 * comparison branch becomes a no-op, modified colors round-trip via wool data.
 */
@Mixin(Wolf.class)
public abstract class WolfDyeMixin {

    @Invoker("getCollarColor")
    abstract DyeColor paperarc$getCollarColor();

    @WrapOperation(
            method = "mobInteract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DyeItem;getDyeColor()Lnet/minecraft/world/item/DyeColor;")
    )
    private DyeColor paperarc$dye(DyeItem dyeItem, Operation<DyeColor> original,
                                  @Local Player player, @Local InteractionHand hand) {
        DyeColor color = original.call(dyeItem);
        EntityDyeEvent event = new EntityDyeEvent(
                PaperArcBridge.bukkitEntity((Wolf) (Object) this),
                org.bukkit.DyeColor.getByWoolData((byte) color.getId()),
                PaperArcBridge.bukkitPlayer(player));
        if (!event.callEvent()) {
            return this.paperarc$getCollarColor();
        }
        return DyeColor.byId(event.getColor().getWoolData());
    }
}
