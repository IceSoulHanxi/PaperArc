package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityDamageItemEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Port of Paper's EntityDamageItemEvent for the equipment-damage path.
 *
 * 1.20.1's generic overload is
 * {@code hurtAndBreak(int, LivingEntity, Consumer<LivingEntity>)} (the
 * 3-arg slot variant does not exist yet). Paper's 1.20.1 patch fires
 * EntityDamageItemEvent for the non-{@code ServerPlayer} branch of this
 * method (vanilla + CraftBukkit route players through PlayerItemDamageEvent).
 * We inject at HEAD of the generic overload: fire EntityDamageItemEvent for
 * non-players, cancel on deny, and re-enter once with the modified amount
 * when changed (guarded by a ThreadLocal so our own re-entry does not
 * re-fire).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackHurtAndBreakMixin {

    @Unique
    private static final ThreadLocal<Boolean> paperarc$reentrant = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @SuppressWarnings("unchecked")
    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void paperarc$entityDamageItem(int amount, LivingEntity entity, Consumer<LivingEntity> breakCallback, CallbackInfo ci) {
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
                ((ItemStack) (Object) this).hurtAndBreak(newAmount, entity, breakCallback);
            } finally {
                paperarc$reentrant.set(Boolean.FALSE);
            }
            ci.cancel();
        }
    }
}
