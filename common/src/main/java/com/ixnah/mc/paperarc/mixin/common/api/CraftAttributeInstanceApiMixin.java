package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_20_R1.attribute.CraftAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's attribute modifier API on CraftAttributeInstance.
 *
 * 1.20.1 paper-api's {@link org.bukkit.attribute.AttributeInstance} only declares
 * addTransientModifier/removeModifier(AttributeModifier)/getModifiers — the
 * UUID- and Key-based lookup/removal overloads are 1.21+ additions and are
 * therefore not implemented here.
 */
@Mixin(CraftAttributeInstance.class)
public abstract class CraftAttributeInstanceApiMixin {

    @Shadow
    private AttributeInstance handle;

    @Unique
    public void addTransientModifier(AttributeModifier modifier) {
        Preconditions.checkArgument(modifier != null, "modifier");
        this.handle.addTransientModifier(CraftAttributeInstance.convert(modifier));
    }
}
