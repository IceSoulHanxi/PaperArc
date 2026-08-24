package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Add-API-to-get-the-collision-shape-of-a-block-before,
 * Add-Destroy-Speed-API and Block-Ticking-API additions on
 * {@link CraftBlockData}: {@code getCollisionShape(Location)},
 * {@code getDestroySpeed(ItemStack, boolean)} and
 * {@code isRandomlyTicked()}.
 */
@Mixin(CraftBlockData.class)
public abstract class CraftBlockDataApiMixin {

    @Shadow
    public abstract net.minecraft.world.level.block.state.BlockState getState();

    @Unique
    public org.bukkit.util.VoxelShape getCollisionShape(Location location) {
        Preconditions.checkArgument(location != null, "location must not be null");
        org.bukkit.craftbukkit.v.CraftWorld world = (org.bukkit.craftbukkit.v.CraftWorld) location.getWorld();
        Preconditions.checkArgument(world != null, "location must not have a null world");
        // Paper: CraftLocation.toBlockPosition(location)
        BlockPos position = org.bukkit.craftbukkit.v.util.CraftLocation.toBlockPosition(location);
        VoxelShape shape = this.getState().getCollisionShape(world.getHandle(), position);
        return new org.bukkit.craftbukkit.v.util.CraftVoxelShape(shape);
    }

    @Unique
    public float getDestroySpeed(final org.bukkit.inventory.ItemStack itemStack, final boolean considerEnchants) {
        // Paper: CraftItemStack.unwrap(itemStack); asNMSCopy is the equivalent
        // present in this CraftBukkit codebase
        ItemStack nmsItemStack = org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(itemStack);
        float speed = nmsItemStack.getDestroySpeed(this.getState());
        if (speed > 1.0F && considerEnchants) {
            final net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute =
                Attributes.MINING_EFFICIENCY;
            // Logic sourced from AttributeInstance#calculateValue (as in Paper)
            final double[] acc = {
                attribute.value().getDefaultValue(), // [0] modified base value
                1.0D,                                // [1] multiplied-base factor
                1.0D                                 // [2] multiplied-total factor
            };
            EnchantmentHelper.forEachModifier(nmsItemStack, EquipmentSlot.MAINHAND,
                (attributeHolder, attributeModifier) -> {
                    switch (attributeModifier.operation()) {
                        case ADD_VALUE -> acc[0] += attributeModifier.amount();
                        case ADD_MULTIPLIED_BASE -> acc[1] += attributeModifier.amount();
                        case ADD_MULTIPLIED_TOTAL -> acc[2] *= (1.0D + attributeModifier.amount());
                    }
                });
            final double actualModifier = acc[0] * acc[1] * acc[2];
            speed += (float) attribute.value().sanitizeValue(actualModifier);
        }
        return speed;
    }

    @Unique
    public boolean isRandomlyTicked() {
        return this.getState().isRandomlyTicking();
    }
}
