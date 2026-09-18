package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.inventory.CreativeCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code CreativeCategory} 实现 adventure 的 {@code Translatable}
 * （checklist §1.12 bb）。
 *
 * <p>paper 把 {@code "itemGroup." + <ctor 形参>} 存进私有 final 字段，运行时枚举的
 * 构造器没有这个形参，只能按常量逐条映射。映射表来自 paper 的 {@code <clinit>}（javap -c）。
 */
@Mixin(CreativeCategory.class)
public abstract class CreativeCategoryApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        String group;
        switch ((CreativeCategory) (Object) this) {
            case BUILDING_BLOCKS:
                group = "buildingBlocks";
                break;
            case DECORATIONS:
                group = "decorations";
                break;
            case REDSTONE:
                group = "redstone";
                break;
            case TRANSPORTATION:
                group = "transportation";
                break;
            case MISC:
                group = "misc";
                break;
            case FOOD:
                group = "food";
                break;
            case TOOLS:
                group = "tools";
                break;
            case COMBAT:
                group = "combat";
                break;
            case BREWING:
                group = "brewing";
                break;
            default:
                group = ((CreativeCategory) (Object) this).name().toLowerCase(java.util.Locale.ENGLISH);
                break;
        }
        return "itemGroup." + group;
    }
}
