package com.ixnah.mc.paperarc.mixin.common.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {

    /**
     * Threads EntityLoadCrossbowEvent#shouldConsumeItem into
     * {@code useAmmo}: when the event asked not to consume the item,
     * every projectile draw behaves like the creative (non-consuming) path.
     * The flag is set by CrossbowItemMixin around the load call and defaults
     * to {@code true}, so non-crossbow callers are unaffected.
     */
    @WrapOperation(
        method = "draw(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/List;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ProjectileWeaponItem;useAmmo(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Z)Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private static ItemStack paperarc$useAmmo(ItemStack weapon, ItemStack candidate, LivingEntity shooter, boolean bl, Operation<ItemStack> original) {
        boolean consume = com.ixnah.mc.paperarc.bridge.CrossbowState.CONSUME_ITEM.get();
        return original.call(weapon, candidate, shooter, bl || !consume);
    }
}
