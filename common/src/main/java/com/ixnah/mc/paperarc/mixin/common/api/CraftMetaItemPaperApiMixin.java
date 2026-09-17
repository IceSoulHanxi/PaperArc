package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.craftbukkit.v.inventory.CraftMetaItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 在 {@code ItemMeta}/{@code meta.Damageable} 上加的几个方法：
 * {@code itemName()}、{@code itemName(Component)}、{@code hasDamageValue()}、{@code resetDamage()}。
 * 全部落在 {@link CraftMetaItem} 上（{@code meta.Damageable} 的实现类就是它）。
 */
@Mixin(CraftMetaItem.class)
public abstract class CraftMetaItemPaperApiMixin {

    @Shadow
    public abstract boolean hasItemName();

    @Shadow
    public abstract String getItemName();

    @Shadow
    public abstract void setItemName(String name);

    @Shadow
    public abstract boolean hasDamage();

    @Shadow
    public abstract void setDamage(int damage);

    @Unique
    public Component itemName() {
        return this.hasItemName()
                ? LegacyComponentSerializer.legacySection().deserialize(this.getItemName())
                : null;
    }

    @Unique
    public void itemName(Component name) {
        this.setItemName(name == null ? null
                : LegacyComponentSerializer.legacySection().serialize(name));
    }

    @Unique
    public boolean hasDamageValue() {
        return this.hasDamage();
    }

    @Unique
    public void resetDamage() {
        this.setDamage(0);
    }
}
