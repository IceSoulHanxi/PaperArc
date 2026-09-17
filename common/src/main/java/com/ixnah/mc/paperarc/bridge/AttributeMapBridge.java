package com.ixnah.mc.paperarc.bridge;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * Cross-class access to the {@code AttributeMap#registerAttribute(Attribute)}
 * method injected by {@code mojmap/entity/AttributeMapFieldsMixin} (Paper's
 * living-entity-allow-attribute-registration patch). Needed because the injected
 * method is only visible through the mixin at runtime, not at compile time on
 * {@code AttributeMap}.
 */
public interface AttributeMapBridge {

    /** 1.21.1 的 AttributeMap 以 {@code Holder<Attribute>} 为键（1.20.1 是 Attribute 本身）。 */
    void registerAttribute(Holder<Attribute> attributeBase);
}
