package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.collect.Multimap;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code Material} 上的方法与 adventure {@code Translatable}（A6/X-2 第三批）。
 * 委托 {@code Bukkit.getUnsafe()}（那些方法运行时由 Arclight 的 CraftMagicNumbers
 * 提供，或本仓库 CraftMagicNumbersApiMixin 已补）。
 */
@Mixin(Material.class)
public abstract class MaterialApiMixin implements Translatable {

    @Unique
    private Material paperarc$self() {
        return (Material) (Object) this;
    }

    @Unique
    public String translationKey() {
        Material self = this.paperarc$self();
        return self.isItem() ? Bukkit.getUnsafe().getItemTranslationKey(self)
                : Bukkit.getUnsafe().getBlockTranslationKey(self);
    }

    /** 与 paper 一致：三种空气都算空。 */
    @Unique
    public boolean isEmpty() {
        switch (this.paperarc$self()) {
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
                return true;
            default:
                return false;
        }
    }

    @Unique
    public boolean isCollidable() {
        return Bukkit.getUnsafe().isCollidable(this.paperarc$self());
    }

    @Unique
    public io.papermc.paper.inventory.ItemRarity getItemRarity() {
        return Bukkit.getUnsafe().getItemRarity(this.paperarc$self());
    }

    @Unique
    public Multimap<Attribute, AttributeModifier> getItemAttributes(EquipmentSlot equipmentSlot) {
        return Bukkit.getUnsafe().getItemAttributes(this.paperarc$self(), equipmentSlot);
    }
}
