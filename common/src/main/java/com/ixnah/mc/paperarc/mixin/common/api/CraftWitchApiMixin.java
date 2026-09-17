package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Witch;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v.entity.CraftWitch;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Witch potion-drinking API (Add-more-Witch-API).
 *
 * Paper's implementation calls {@code getHandle().usingTime} and a
 * {@code setDrinkingPotion(ItemStack)} helper that Paper itself adds to the NMS class.
 * Vanilla NMS has neither, so this mixin mirrors the helper body locally: private
 * {@code usingTime} and the drinking speed modifier statics are opened by
 * {@code paperarc.accesswidener}, and the witch-ready-potion event hook
 * (Paper-only CraftEventFactory method) is omitted — vanilla behavior otherwise preserved.
 */
@Mixin(CraftWitch.class)
public abstract class CraftWitchApiMixin {

    @Shadow
    public abstract Witch getHandle();

    @Unique
    public ItemStack getDrinkingPotion() {
        return CraftItemStack.asCraftMirror(getHandle().getMainHandItem());
    }

    @Unique
    public void setDrinkingPotion(ItemStack potion) {
        // 注意：paper 的 Material#isEmpty() 在 Arclight 1.21.1 运行时不存在（NoSuchMethodError，
        // 真机实测），实现体里不能用 paper-only 的 API —— 这里退回 Material.AIR 比较。
        Preconditions.checkArgument(potion == null || potion.getType() == Material.AIR || potion.getType() == Material.POTION, "must be potion, air, or null");
        Witch witch = getHandle();
        // Mirror of Paper's NMS Witch#setDrinkingPotion body (minus its event hook).
        witch.setItemSlot(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(potion));
        witch.usingTime = witch.getMainHandItem().getUseDuration(witch);
        witch.setUsingItem(true);
        if (!witch.isSilent()) {
            witch.level().playSound(null, witch.getX(), witch.getY(), witch.getZ(),
                SoundEvents.WITCH_DRINK, witch.getSoundSource(), 1.0F, 0.8F + witch.getRandom().nextFloat() * 0.4F);
        }
        AttributeInstance movementSpeed = witch.getAttribute(Attributes.MOVEMENT_SPEED);
        movementSpeed.removeModifier(Witch.SPEED_MODIFIER_DRINKING_ID);
        movementSpeed.addTransientModifier(Witch.SPEED_MODIFIER_DRINKING);
    }

    @Unique
    public int getPotionUseTimeLeft() {
        return getHandle().usingTime;
    }

    @Unique
    public void setPotionUseTimeLeft(int ticks) {
        getHandle().usingTime = ticks;
    }
}
