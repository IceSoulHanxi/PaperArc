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
 * {@code usingTime} and the drinking speed modifier statics are widened via AT
 * (f_34129_ / f_34126_ / f_34127_) and accessed directly — no reflection. The
 * witch-ready-potion event hook (Paper-only CraftEventFactory method) is omitted.
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
        Preconditions.checkArgument(potion == null || potion.getType().isEmpty() || potion.getType() == Material.POTION, "must be potion, air, or null");
        Witch witch = getHandle();
        // Mirror of Paper's NMS Witch#setDrinkingPotion body (minus its event hook).
        witch.setItemSlot(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(potion));
        witch.usingTime = witch.getMainHandItem().getUseDuration();
        witch.setUsingItem(true);
        if (!witch.isSilent()) {
            witch.level().playSound(null, witch.getX(), witch.getY(), witch.getZ(),
                SoundEvents.WITCH_DRINK, witch.getSoundSource(), 1.0F, 0.8F + witch.getRandom().nextFloat() * 0.4F);
        }
        // SPEED_MODIFIER_DRINKING_UUID / SPEED_MODIFIER_DRINKING widened via AT.
        AttributeModifier modifier = Witch.SPEED_MODIFIER_DRINKING;
        AttributeInstance movementSpeed = witch.getAttribute(Attributes.MOVEMENT_SPEED);
        movementSpeed.removeModifier(modifier.getId());
        movementSpeed.addTransientModifier(modifier);
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
