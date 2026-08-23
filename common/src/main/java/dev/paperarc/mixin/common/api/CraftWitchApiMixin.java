package dev.paperarc.mixin.common.api;

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
 * {@code usingTime} and the drinking speed modifier statics are read reflectively,
 * and the witch-ready-potion event hook (Paper-only CraftEventFactory method) is
 * omitted — vanilla behavior otherwise preserved.
 */
@Mixin(CraftWitch.class)
public abstract class CraftWitchApiMixin {

    @Shadow
    public abstract Witch getHandle();

    @Unique
    private static int paperarc$usingTime(Witch witch) {
        try {
            java.lang.reflect.Field f = Witch.class.getDeclaredField("usingTime");
            f.setAccessible(true);
            return f.getInt(witch);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS Witch.usingTime field not found", e);
        }
    }

    @Unique
    private static void paperarc$setUsingTime(Witch witch, int ticks) {
        try {
            java.lang.reflect.Field f = Witch.class.getDeclaredField("usingTime");
            f.setAccessible(true);
            f.setInt(witch, ticks);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS Witch.usingTime field not found", e);
        }
    }

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
        paperarc$setUsingTime(witch, witch.getMainHandItem().getUseDuration(witch));
        witch.setUsingItem(true);
        if (!witch.isSilent()) {
            witch.level().playSound(null, witch.getX(), witch.getY(), witch.getZ(),
                SoundEvents.WITCH_DRINK, witch.getSoundSource(), 1.0F, 0.8F + witch.getRandom().nextFloat() * 0.4F);
        }
        try {
            java.lang.reflect.Field idField = Witch.class.getDeclaredField("SPEED_MODIFIER_DRINKING_ID");
            idField.setAccessible(true);
            java.lang.reflect.Field modField = Witch.class.getDeclaredField("SPEED_MODIFIER_DRINKING");
            modField.setAccessible(true);
            ResourceLocation modifierId = (ResourceLocation) idField.get(null);
            AttributeModifier modifier = (AttributeModifier) modField.get(null);
            AttributeInstance movementSpeed = witch.getAttribute(Attributes.MOVEMENT_SPEED);
            movementSpeed.removeModifier(modifierId);
            movementSpeed.addTransientModifier(modifier);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS Witch drinking speed modifier not found", e);
        }
    }

    @Unique
    public int getPotionUseTimeLeft() {
        return paperarc$usingTime(getHandle());
    }

    @Unique
    public void setPotionUseTimeLeft(int ticks) {
        paperarc$setUsingTime(getHandle(), ticks);
    }
}
