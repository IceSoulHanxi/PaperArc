package com.ixnah.mc.paperarc.bridge;

import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * Cross-class access to the {@code AttributeMap#registerAttribute(Attribute)}
 * method injected by {@code mojmap/entity/AttributeMapFieldsMixin} (Paper's
 * living-entity-allow-attribute-registration patch). Needed because the injected
 * method is only visible through the mixin at runtime, not at compile time on
 * {@code AttributeMap}.
 */
public interface AttributeMapBridge {

    void registerAttribute(Attribute attributeBase);
}
