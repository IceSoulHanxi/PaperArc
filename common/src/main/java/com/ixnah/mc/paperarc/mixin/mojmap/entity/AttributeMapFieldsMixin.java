package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import java.util.Map;

import com.ixnah.mc.paperarc.bridge.AttributeMapBridge;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mirrors Paper's {@code living-entity-allow-attribute-registration.patch}: exposes
 * {@code AttributeMap#registerAttribute(Attribute)} so that
 * {@code org.bukkit.attribute.Attributable#registerAttribute(Attribute)} can be
 * implemented on {@code CraftLivingEntity}.
 *
 * <p>Vanilla 1.20.1 only wires attributes from the {@code AttributeSupplier}; Paper
 * adds this public mutator which directly inserts a fresh {@code AttributeInstance}
 * into the backing map. Accessed cross-class via {@link AttributeMapBridge}.</p>
 */
@Mixin(AttributeMap.class)
public abstract class AttributeMapFieldsMixin implements AttributeMapBridge {

    @Shadow
    @Final
    private Map<Attribute, AttributeInstance> attributes;

    @Unique
    @Override
    public void registerAttribute(Attribute attributeBase) {
        AttributeInstance attributeModifiable = new AttributeInstance(attributeBase, AttributeInstance::getAttribute);
        this.attributes.put(attributeBase, attributeModifiable);
    }
}
