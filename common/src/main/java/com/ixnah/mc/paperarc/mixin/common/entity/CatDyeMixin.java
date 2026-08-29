package com.ixnah.mc.paperarc.mixin.common.entity;

import io.papermc.paper.event.entity.EntityDyeEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's EntityDyeEvent for Cat collar dyeing.
 * Paper inserts the event between the collar-color comparison and the color
 * set inside mobInteract; we wrap the DyeItem.getDyeColor() call instead:
 * a cancelled event returns the current collar color so the vanilla
 * {@code enumcolor != getCollarColor()} branch becomes a no-op, and a
 * modified color is converted back via wool data exactly like Paper.
 */
@Mixin(Cat.class)
public abstract class CatDyeMixin {

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
                PaperArcBridge.bukkitEntity((Cat) (Object) this),
                org.bukkit.DyeColor.getByWoolData((byte) color.getId()),
                PaperArcBridge.bukkitPlayer(player));
        if (!event.callEvent()) {
            return this.paperarc$getCollarColor();
        }
        return DyeColor.byId(event.getColor().getWoolData());
    }
}
