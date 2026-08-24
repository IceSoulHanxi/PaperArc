package com.ixnah.mc.paperarc.mixin.common.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    /**
     * Whether the currently-loading crossbow should consume its ammunition.
     * Set from {@link #paperarc$loadCrossbow} based on
     * EntityLoadCrossbowEvent#shouldConsumeItem(); consumed by
     * ProjectileWeaponItemMixin inside draw(). Defaults to {@code true}
     * so any path that never fires the event keeps vanilla behavior.
     */
    

    @WrapOperation(
        method = "releaseUsing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/CrossbowItem;tryLoadProjectiles(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Z"
        )
    )
    private boolean paperarc$loadCrossbow(LivingEntity shooter, ItemStack crossbow, Operation<Boolean> original) {
        EntityLoadCrossbowEvent event = new EntityLoadCrossbowEvent(
            (org.bukkit.entity.LivingEntity) PaperArcBridge.bukkitEntity(shooter),
            CraftItemStack.asCraftMirror(crossbow),
            CraftEquipmentSlot.getHand(shooter.getUsedItemHand())
        );
        if (!event.callEvent()) {
            paperarc$syncInventory(shooter);
            return false;
        }
        com.ixnah.mc.paperarc.bridge.CrossbowState.CONSUME_ITEM.set(event.shouldConsumeItem());
        try {
            boolean loaded = original.call(shooter, crossbow);
            if (!loaded || !event.shouldConsumeItem()) {
                // Paper also resends inventory when the load failed or the
                // item was not consumed, then skips the charged sounds.
                paperarc$syncInventory(shooter);
                return false;
            }
            return true;
        } finally {
            com.ixnah.mc.paperarc.bridge.CrossbowState.CONSUME_ITEM.remove();
        }
    }

    private static void paperarc$syncInventory(LivingEntity user) {
        if (((Object) user) instanceof ServerPlayer player) {
            player.containerMenu.sendAllDataToRemote();
        }
    }
}
