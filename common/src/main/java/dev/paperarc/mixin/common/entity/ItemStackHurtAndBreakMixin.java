package dev.paperarc.mixin.common.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityDamageItemEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's EntityDamageItemEvent for the equipment-damage path.
 *
 * Runtime server is VANILLA 1.21.1: the generic-entity overload is
 * {@code hurtAndBreak(int, LivingEntity, EquipmentSlot)} (players are routed
 * onward to the (int, ServerLevel, ServerPlayer, Consumer) overload which
 * keeps the CraftBukkit PlayerItemDamageEvent path). We inject at HEAD of the
 * generic overload: fire EntityDamageItemEvent for non-players, cancel on
 * deny, and re-enter once with the modified amount when changed (guarded by a
 * ThreadLocal so our own re-entry does not re-fire).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackHurtAndBreakMixin {

    @Unique
    private static final ThreadLocal<Boolean> paperarc$reentrant = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void paperarc$entityDamageItem(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        if (paperarc$reentrant.get() == Boolean.TRUE || entity instanceof ServerPlayer) {
            return;
        }
        EntityDamageItemEvent event = new EntityDamageItemEvent(
                PaperArcBridge.bukkitEntity(entity),
                CraftItemStack.asCraftMirror((ItemStack) (Object) this),
                amount);
        if (!event.callEvent()) {
            ci.cancel();
            return;
        }
        int newAmount = event.getDamage();
        if (newAmount != amount) {
            paperarc$reentrant.set(Boolean.TRUE);
            try {
                ((ItemStack) (Object) this).hurtAndBreak(newAmount, entity, slot);
            } finally {
                paperarc$reentrant.set(Boolean.FALSE);
            }
            ci.cancel();
        }
    }
}
