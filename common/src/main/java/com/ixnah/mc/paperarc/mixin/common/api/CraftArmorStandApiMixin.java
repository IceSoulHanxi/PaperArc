package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.entity.CraftArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's extended ArmorStand API on {@link CraftArmorStand}
 * (Expand-ArmorStand-API + can-move + can-tick patches).
 *
 * <p>Rotations map directly onto vanilla {@code net.minecraft.core.Rotations}
 * pose getters/setters (public in mojmap). {@code disabledSlots} is private in
 * vanilla NMS and there is no access-widener here, so it is read/written via
 * reflection using vanilla's own bit layout. {@code canMove}/{@code canTick}
 * are Paper-added NMS fields that do not exist in this vanilla-based runtime,
 * so they live in the ApiState side map keyed by the NMS handle.</p>
 */
@Mixin(CraftArmorStand.class)
public abstract class CraftArmorStandApiMixin {

    @Shadow
    public abstract net.minecraft.world.entity.decoration.ArmorStand getHandle();

    @Unique
    public ItemStack getItem(EquipmentSlot slot) {
        com.google.common.base.Preconditions.checkArgument(slot != null, "slot");
        com.google.common.base.Preconditions.checkArgument(slot != EquipmentSlot.BODY, "Cannot get body item");
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(
                this.getHandle().getItemBySlot(CraftEquipmentSlot.getNMS(slot)));
    }

    @Unique
    public void setItem(EquipmentSlot slot, ItemStack item) {
        com.google.common.base.Preconditions.checkArgument(slot != null, "slot");
        com.google.common.base.Preconditions.checkArgument(slot != EquipmentSlot.BODY, "Cannot set body item");
        org.bukkit.inventory.EntityEquipment equipment = ((org.bukkit.entity.LivingEntity) (Object) this).getEquipment();
        switch (slot) {
            case HAND:
                equipment.setItemInMainHand(item);
                return;
            case OFF_HAND:
                equipment.setItemInOffHand(item);
                return;
            case FEET:
                equipment.setBoots(item);
                return;
            case LEGS:
                equipment.setLeggings(item);
                return;
            case CHEST:
                equipment.setChestplate(item);
                return;
            case HEAD:
                equipment.setHelmet(item);
                return;
        }
        throw new UnsupportedOperationException(slot.name());
    }

    // vanilla ArmorStand.disabledSlots is private and there is no access widener here
    @Unique
    private static java.lang.reflect.Field paperarc$disabledSlotsField;

    @Unique
    private static java.lang.reflect.Field paperarc$disabledSlotsField() {
        if (paperarc$disabledSlotsField == null) {
            try {
                java.lang.reflect.Field f = net.minecraft.world.entity.decoration.ArmorStand.class.getDeclaredField("disabledSlots");
                f.setAccessible(true);
                paperarc$disabledSlotsField = f;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("paperarc: cannot reflect ArmorStand.disabledSlots", e);
            }
        }
        return paperarc$disabledSlotsField;
    }

    @Unique
    private int paperarc$getRawDisabledSlots() {
        try {
            return paperarc$disabledSlotsField().getInt(this.getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("paperarc: cannot read ArmorStand.disabledSlots", e);
        }
    }

    @Unique
    public java.util.Set<EquipmentSlot> getDisabledSlots() {
        java.util.Set<EquipmentSlot> disabled = new java.util.HashSet<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (this.isSlotDisabled(slot)) {
                disabled.add(slot);
            }
        }
        return disabled;
    }

    @Unique
    public boolean isSlotDisabled(EquipmentSlot slot) {
        // vanilla ArmorStand#isDisabled is private; replicate its formula on the reflected field
        return (this.paperarc$getRawDisabledSlots() & (1 << CraftEquipmentSlot.getNMS(slot).getFilterFlag())) != 0;
    }

    @Unique
    public void setDisabledSlots(EquipmentSlot... slots) {
        int disabled = 0;
        for (EquipmentSlot slot : slots) {
            if (slot == EquipmentSlot.OFF_HAND) continue;
            net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
            disabled += (1 << nmsSlot.getFilterFlag()) + (1 << (nmsSlot.getFilterFlag() + 8)) + (1 << (nmsSlot.getFilterFlag() + 16));
        }
        try {
            paperarc$disabledSlotsField().setInt(this.getHandle(), disabled);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("paperarc: cannot write ArmorStand.disabledSlots", e);
        }
    }

    @Unique
    public void addDisabledSlots(EquipmentSlot... slots) {
        java.util.Set<EquipmentSlot> disabled = this.getDisabledSlots();
        java.util.Collections.addAll(disabled, slots);
        this.setDisabledSlots(disabled.toArray(new EquipmentSlot[0]));
    }

    @Unique
    public void removeDisabledSlots(EquipmentSlot... slots) {
        java.util.Set<EquipmentSlot> disabled = this.getDisabledSlots();
        for (final EquipmentSlot slot : slots) disabled.remove(slot);
        this.setDisabledSlots(disabled.toArray(new EquipmentSlot[0]));
    }

    @Unique
    public io.papermc.paper.math.Rotations getBodyRotations() {
        return this.paperarc$fromNMS(this.getHandle().getBodyPose());
    }

    @Unique
    public void setBodyRotations(io.papermc.paper.math.Rotations rotations) {
        this.getHandle().setBodyPose(this.paperarc$toNMS(rotations));
    }

    @Unique
    public io.papermc.paper.math.Rotations getLeftArmRotations() {
        return this.paperarc$fromNMS(this.getHandle().getLeftArmPose());
    }

    @Unique
    public void setLeftArmRotations(io.papermc.paper.math.Rotations rotations) {
        this.getHandle().setLeftArmPose(this.paperarc$toNMS(rotations));
    }

    @Unique
    public io.papermc.paper.math.Rotations getRightArmRotations() {
        return this.paperarc$fromNMS(this.getHandle().getRightArmPose());
    }

    @Unique
    public void setRightArmRotations(io.papermc.paper.math.Rotations rotations) {
        this.getHandle().setRightArmPose(this.paperarc$toNMS(rotations));
    }

    @Unique
    public io.papermc.paper.math.Rotations getHeadRotations() {
        return this.paperarc$fromNMS(this.getHandle().getHeadPose());
    }

    @Unique
    public void setHeadRotations(io.papermc.paper.math.Rotations rotations) {
        this.getHandle().setHeadPose(this.paperarc$toNMS(rotations));
    }

    @Unique
    public io.papermc.paper.math.Rotations getLeftLegRotations() {
        return this.paperarc$fromNMS(this.getHandle().getLeftLegPose());
    }

    @Unique
    public void setLeftLegRotations(io.papermc.paper.math.Rotations rotations) {
        this.getHandle().setLeftLegPose(this.paperarc$toNMS(rotations));
    }

    @Unique
    public io.papermc.paper.math.Rotations getRightLegRotations() {
        return this.paperarc$fromNMS(this.getHandle().getRightLegPose());
    }

    @Unique
    public void setRightLegRotations(io.papermc.paper.math.Rotations rotations) {
        this.getHandle().setRightLegPose(this.paperarc$toNMS(rotations));
    }

    @Unique
    public boolean canMove() {
        // Paper stores this as an NMS field (public boolean canMove); not present in vanilla runtime -> side-map
        return com.ixnah.mc.paperarc.bridge.ApiState.get(this.getHandle(), "paperarc.canMove", Boolean.TRUE);
    }

    @Unique
    public void setCanMove(boolean move) {
        com.ixnah.mc.paperarc.bridge.ApiState.put(this.getHandle(), "paperarc.canMove", move);
    }

    @Unique
    public boolean canTick() {
        // Paper default: world.paperConfig().entities.armorStands.tick (defaults to true)
        return com.ixnah.mc.paperarc.bridge.ApiState.get(this.getHandle(), "paperarc.canTick", Boolean.TRUE);
    }

    @Unique
    public void setCanTick(boolean tick) {
        // Paper also sets canTickSetByAPI=true for NBT persistence; side-map has no persistence layer
        com.ixnah.mc.paperarc.bridge.ApiState.put(this.getHandle(), "paperarc.canTick", tick);
    }

    @Unique
    private static io.papermc.paper.math.Rotations paperarc$fromNMS(net.minecraft.core.Rotations old) {
        return io.papermc.paper.math.Rotations.ofDegrees(old.getX(), old.getY(), old.getZ());
    }

    @Unique
    private static net.minecraft.core.Rotations paperarc$toNMS(io.papermc.paper.math.Rotations old) {
        return new net.minecraft.core.Rotations((float) old.x(), (float) old.y(), (float) old.z());
    }
}
