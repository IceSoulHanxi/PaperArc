package dev.paperarc.mixin.common.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityDamageItemEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

/**
 * Port of Paper's EntityDamageItemEvent for the equipment-damage path.
 * Paper widens {@code ItemStack.hurtAndBreak} to accept a LivingEntity and
 * fires the event for any non-ServerPlayer entity. Here (NeoForge layout)
 * {@code ItemStack.hurtAndBreak(int, LivingEntity, EquipmentSlot)} already
 * forwards the living entity to the generic overload; we intercept that call
 * so shields/armor breaking by mobs etc. fire the event. Players keep the
 * CraftBukkit PlayerItemDamageEvent path.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackHurtAndBreakMixin {

    @WrapOperation(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V")
    )
    private void paperarc$entityDamageItem(ItemStack instance, int amount, ServerLevel level, LivingEntity entity,
                                           Consumer<Item> onBreak, Operation<Void> original) {
        if (entity instanceof ServerPlayer) {
            original.call(instance, amount, level, entity, onBreak);
            return;
        }
        EntityDamageItemEvent event = new EntityDamageItemEvent(
                PaperArcBridge.bukkitEntity(entity), CraftItemStack.asCraftMirror(instance), amount);
        if (event.callEvent()) {
            original.call(instance, event.getDamage(), level, entity, onBreak);
        }
    }
}
