package dev.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerArmorChangeEvent
 * (Add-PlayerArmorChangeEvent.patch).
 *
 * Paper injects inside LivingEntity.collectEquipmentChanges' equipment-diff
 * loop: when equipmentHasChanged(old, new) is true and the entity is a
 * ServerPlayer wearing HUMANOID_ARMOR, fires
 * PlayerArmorChangeEvent(player, SlotType.valueOf(slot.name()),
 * asBukkitCopy(old), asBukkitCopy(new)).
 *
 * We wrap the single equipmentHasChanged INVOKE (bytecode-verified 1.21.1:
 * exactly one call site, with the EquipmentSlot local in scope).
 * Arclight's LivingEntityMixin does not touch collectEquipmentChanges.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorChangeMixin {

    @WrapOperation(
            method = "collectEquipmentChanges",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;equipmentHasChanged(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean paperarc$armorChange(ItemStack oldItem, ItemStack newItem, Operation<Boolean> original,
                                         @Local EquipmentSlot slot) {
        boolean changed = original.call(oldItem, newItem);
        if (changed
                && (Object) this instanceof ServerPlayer serverPlayer
                && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            PlayerArmorChangeEvent event = new PlayerArmorChangeEvent(
                    PaperArcBridge.bukkitPlayer(serverPlayer),
                    PlayerArmorChangeEvent.SlotType.valueOf(slot.name()),
                    CraftItemStack.asBukkitCopy(oldItem),
                    CraftItemStack.asBukkitCopy(newItem));
            PaperArcBridge.fire(event);
        }
        return changed;
    }
}
