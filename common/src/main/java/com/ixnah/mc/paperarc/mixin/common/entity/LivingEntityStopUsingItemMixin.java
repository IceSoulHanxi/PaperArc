package com.ixnah.mc.paperarc.mixin.common.entity;

import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's Add-PlayerStopUsingItemEvent patch.
 *
 * Host is {@link LivingEntity} (not a player class), matching the patch site.
 * Fires {@link PlayerStopUsingItemEvent} (non-cancellable) at HEAD of
 * {@code releaseUsingItem} when the entity is a ServerPlayer with a non-empty
 * {@code useItem}, before {@code useItem.releaseUsing(...)} — identical guard
 * and ordering to Paper. The item is exposed as a live mirror via
 * {@code CraftItemStack.asCraftMirror} (equivalent of Paper's
 * {@code ItemStack.asBukkitMirror()}, a Paper-only alias).
 */
@Mixin(LivingEntity.class)
public class LivingEntityStopUsingItemMixin {

    @Shadow
    protected ItemStack useItem;

    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    private void paperarc$fireStopUsingItem(CallbackInfo ci) {
        if (((Object) this instanceof ServerPlayer serverPlayer) && !this.useItem.isEmpty()) {
            PaperArcBridge.fire(new PlayerStopUsingItemEvent(
                PaperArcBridge.bukkitPlayer(serverPlayer),
                CraftItemStack.asCraftMirror(this.useItem),
                ((LivingEntity) (Object) this).getTicksUsingItem()
            ));
        }
    }
}
