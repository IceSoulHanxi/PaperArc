package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v.attribute.CraftAttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code Attributable#registerAttribute(Attribute)} 的第二个实现类：
 * 除了 CraftLivingEntity，{@code CraftAttributeMap} 也实现 Attributable。
 */
@Mixin(CraftAttributeMap.class)
public abstract class CraftAttributeMapApiMixin {

    @Shadow
    private net.minecraft.world.entity.ai.attributes.AttributeMap handle;

    @Unique
    public void registerAttribute(Attribute attribute) {
        com.google.common.base.Preconditions.checkArgument(attribute != null, "attribute cannot be null");
        net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> holder =
                org.bukkit.craftbukkit.v.attribute.CraftAttribute.bukkitToMinecraftHolder(attribute);
        if (this.handle.hasAttribute(holder)) {
            return;
        }
        this.handle.attributes.put(holder,
                new net.minecraft.world.entity.ai.attributes.AttributeInstance(holder, instance -> { }));
    }
}
