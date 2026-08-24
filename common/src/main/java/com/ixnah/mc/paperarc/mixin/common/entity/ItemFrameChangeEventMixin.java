package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's Add-PlayerItemFrameChangeEvent patch.
 * <p>
 * Host: {@link ItemFrame} (not a player class, hence the entity package).
 * <p>
 * Injection points chosen to avoid Arclight's ItemFrameMixin, which already
 * injects at {@code INVOKE dropItem} inside {@code hurt} and overwrites the
 * 3-arg {@code setItem}: we anchor the REMOVE case one instruction earlier
 * (first {@code DamageSource.getEntity()} call in {@code hurt}) and never
 * touch {@code setItem} itself.
 */
@Mixin(ItemFrame.class)
public abstract class ItemFrameChangeEventMixin {

    @Shadow
    public abstract net.minecraft.world.item.ItemStack getItem();

    @Shadow
    public abstract void setItem(net.minecraft.world.item.ItemStack stack, boolean updateComparator);

    @Inject(
        method = "hurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;",
            ordinal = 0
        ),
        cancellable = true
    )
    private void paperarc$onRemove(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof Player player) {
            PlayerItemFrameChangeEvent event = new PlayerItemFrameChangeEvent(
                PaperArcBridge.bukkitPlayer(player),
                PaperArcBridge.bukkitEntity((ItemFrame) (Object) this),
                org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(this.getItem()),
                PlayerItemFrameChangeEvent.ItemFrameChangeAction.REMOVE
            );
            if (!event.callEvent()) {
                // return true: the damage itself is not cancelled, only the change
                cir.setReturnValue(true);
                return;
            }
            this.setItem(org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(event.getItemStack()), false);
        }
    }

    @Inject(
        method = "interact",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/decoration/ItemFrame;setItem(Lnet/minecraft/world/item/ItemStack;)V"
        ),
        cancellable = true
    )
    private void paperarc$onPlace(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemstack = player.getItemInHand(hand);
        PlayerItemFrameChangeEvent event = new PlayerItemFrameChangeEvent(
            PaperArcBridge.bukkitPlayer(player),
            PaperArcBridge.bukkitEntity((ItemFrame) (Object) this),
            org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(itemstack),
            PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE
        );
        if (!event.callEvent()) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        // Replicate the vanilla tail (setItem + gameEvent + consume + return CONSUME)
        // so the event's (possibly modified) ItemStack is what actually gets placed.
        this.setItem(org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(event.getItemStack()), true);
        ((net.minecraft.world.entity.Entity) (Object) this).gameEvent(net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, player);
        itemstack.consume(1, player);
        cir.setReturnValue(InteractionResult.CONSUME);
    }

    @Inject(
        method = "interact",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/decoration/ItemFrame;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
        ),
        cancellable = true
    )
    private void paperarc$onRotate(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        PlayerItemFrameChangeEvent event = new PlayerItemFrameChangeEvent(
            PaperArcBridge.bukkitPlayer(player),
            PaperArcBridge.bukkitEntity((ItemFrame) (Object) this),
            org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(this.getItem()),
            PlayerItemFrameChangeEvent.ItemFrameChangeAction.ROTATE
        );
        if (!event.callEvent()) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        this.setItem(org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(event.getItemStack()), false);
        // vanilla continues: rotate sound, rotation+1, gameEvent, return CONSUME
    }
}
