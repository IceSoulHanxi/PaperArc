package com.ixnah.mc.paperarc.bridge.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.EntityTypeTags;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v.entity.CraftEntityType;
import org.bukkit.entity.EntityCategory;

/**
 * paper 的 {@code Enchantment#getDamageIncrease(int, EntityCategory/EntityType)}。
 *
 * <p>1.21 之后附魔的额外伤害由 {@code damage} 效果组件按战斗上下文算
 *（{@code Enchantment#modifyDamage} 要 ServerLevel + ItemStack + 目标 + DamageSource），
 * 没有"按实体类别给固定加成"的模型可查；Paper 在 1.21 上也是照 1.20 的公式硬编码。
 * 这里抄同一张表，并且 {@code EntityType} 那个重载用 vanilla 1.21.1 真正在用的
 * {@code #minecraft:sensitive_to_*} 标签判定，比按 Bukkit 的 {@code EntityCategory}
 * 更贴运行时行为。
 *
 * <p>公式（vanilla 1.20 的 {@code DamageEnchantment}/{@code TridentImpalerEnchantment}）：
 * 锋利 {@code 1.0 + max(0, level-1) * 0.5}（任何目标）；亡灵杀手/节肢杀手/穿刺
 * 对各自类别 {@code level * 2.5}，其余 0。
 */
public final class PaperarcEnchantmentDamage {

    private PaperarcEnchantmentDamage() {
    }

    public static float byCategory(NamespacedKey enchantment, int level, EntityCategory category) {
        if (enchantment == null) {
            return 0.0F;
        }
        return switch (enchantment.getKey()) {
            case "sharpness" -> 1.0F + Math.max(0, level - 1) * 0.5F;
            case "smite" -> category == EntityCategory.UNDEAD ? level * 2.5F : 0.0F;
            case "bane_of_arthropods" -> category == EntityCategory.ARTHROPOD ? level * 2.5F : 0.0F;
            case "impaling" -> category == EntityCategory.WATER ? level * 2.5F : 0.0F;
            default -> 0.0F;
        };
    }

    public static float byEntityType(NamespacedKey enchantment, int level, org.bukkit.entity.EntityType entityType) {
        if (enchantment == null) {
            return 0.0F;
        }
        if ("sharpness".equals(enchantment.getKey())) {
            return 1.0F + Math.max(0, level - 1) * 0.5F;
        }
        if (entityType == null) {
            return 0.0F;
        }
        net.minecraft.world.entity.EntityType<?> nms = CraftEntityType.bukkitToMinecraft(entityType);
        if (nms == null) {
            return 0.0F;
        }
        net.minecraft.core.Holder<net.minecraft.world.entity.EntityType<?>> holder =
                BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(nms);
        return switch (enchantment.getKey()) {
            case "smite" -> holder.is(EntityTypeTags.SENSITIVE_TO_SMITE) ? level * 2.5F : 0.0F;
            case "bane_of_arthropods" ->
                    holder.is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS) ? level * 2.5F : 0.0F;
            case "impaling" -> holder.is(EntityTypeTags.SENSITIVE_TO_IMPALING) ? level * 2.5F : 0.0F;
            default -> 0.0F;
        };
    }
}
