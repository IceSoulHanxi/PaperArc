package com.ixnah.mc.paperarc.mixin.common.entity;

import java.util.Map;

import com.ixnah.mc.paperarc.bridge.AttributeMapBridge;
import net.minecraft.core.Holder;
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
 * <p>1.21.1 起 {@code AttributeMap} 的键类型是 {@code Holder<Attribute>}（1.20.1 是
 * {@code Attribute} 本身），构造 {@code AttributeInstance} 也要传 Holder。</p>
 *
 * <p>Vanilla only wires attributes from the {@code AttributeSupplier}; Paper
 * adds this public mutator which directly inserts a fresh {@code AttributeInstance}
 * into the backing map. Accessed cross-class via {@link AttributeMapBridge}.</p>
 */
@Mixin(AttributeMap.class)
public abstract class AttributeMapFieldsMixin implements AttributeMapBridge {

    @Unique
    @Override
    public void registerAttribute(Holder<Attribute> attributeBase) {
        AttributeInstance attributeModifiable = new AttributeInstance(attributeBase, AttributeInstance::getAttribute);
        // 同 AbstractFurnaceBlockEntityFieldsMixin：泛型字段不走 @Shadow，由 accesswidener 放开后直接访问
        ((AttributeMap) (Object) this).attributes.put(attributeBase, attributeModifiable);
    }
}
