package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import io.papermc.paper.inventory.ItemRarity;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;

/**
 * paper 加在 {@code Material} 枚举上的 6 个方法（checklist §1.10 am，第 ⑤ 批）。
 *
 * <p>{@code isEmpty()} paper 编译成了按 {@code ordinal()} 的 tableswitch，
 * 那是 paper 自己那份枚举的常量序号，照搬到运行时枚举上是错的；这里按语义写成
 * 三个常量比较（AIR / CAVE_AIR / VOID_AIR，与 paper 的三个 case 一一对应）。</p>
 */
@Mixin(Material.class)
public abstract class MaterialApiMixin {

    @Unique
    private Material paperarc$self() {
        return (Material) (Object) this;
    }

    @Unique
    public boolean isEmpty() {
        Material self = this.paperarc$self();
        return self == Material.AIR || self == Material.CAVE_AIR || self == Material.VOID_AIR;
    }

    @Unique
    public String translationKey() {
        Material self = this.paperarc$self();
        return self.isItem()
                ? Objects.requireNonNull(self.asItemType()).translationKey()
                : Objects.requireNonNull(self.asBlockType()).translationKey();
    }

    @Unique
    public ItemRarity getItemRarity() {
        return new ItemStack(this.paperarc$self()).getRarity();
    }

    @Unique
    public Multimap<Attribute, AttributeModifier> getItemAttributes(EquipmentSlot equipmentSlot) {
        return this.paperarc$self().getDefaultAttributeModifiers(equipmentSlot);
    }

    @Unique
    public boolean isCollidable() {
        Material self = this.paperarc$self();
        if (!self.isBlock()) {
            throw new IllegalArgumentException(self + " isn't a block type");
        }
        BlockType type = self.asBlockType();
        return Objects.requireNonNull(type).hasCollision();
    }

    @Unique
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers() {
        ItemType type = this.paperarc$self().asItemType();
        Preconditions.checkArgument(type != null, "The Material is not an item!");
        return type.getDefaultAttributeModifiers();
    }
}
