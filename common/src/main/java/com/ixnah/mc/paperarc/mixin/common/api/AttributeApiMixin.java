package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.attribute.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 让 {@code Attribute} 实现 adventure {@code Translatable}（A6/X-2 第三批）。 */
@Mixin(Attribute.class)
public abstract class AttributeApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return "attribute.name." + ((Attribute) (Object) this).getKey().getKey();
    }
}
