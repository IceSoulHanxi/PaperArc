package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.AttributeMapBridge;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.bukkit.craftbukkit.v.attribute.CraftAttributeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code Attributable} 的第二个实现类。
 *
 * <p>{@code registerAttribute(Attribute)} 的实现体原先只挂在 {@code CraftLivingEntity} 上，
 * 而 {@code CraftAttributeMap}（{@code ItemMeta}/{@code Attributable} 的独立实现，
 * 不继承 CraftEntity）走到就是 {@code AbstractMethodError}（A4-1 r）。
 */
@Mixin(CraftAttributeMap.class)
public abstract class CraftAttributeMapApiMixin {

    @Shadow
    @Final
    private AttributeMap handle;

    @Unique
    public void registerAttribute(org.bukkit.attribute.Attribute attribute) {
        Preconditions.checkArgument(attribute != null, "attribute");
        ((AttributeMapBridge) this.handle).registerAttribute(CraftAttributeMap.toMinecraft(attribute));
    }
}
