package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.inventory.CreativeCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code CreativeCategory} 实现 adventure {@code Translatable}（A6/X-2 第三批）。
 * paper 把 key 放在自己加的私有字段里，运行时没有，取值从 paper-api 的 {@code <clinit>}
 * 逐个 javap 出来写死（BUILDING_BLOCKS=buildingBlocks，其余即小写枚举名）。
 */
@Mixin(CreativeCategory.class)
public abstract class CreativeCategoryApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        String key;
        switch ((CreativeCategory) (Object) this) {
            case BUILDING_BLOCKS:
                key = "buildingBlocks";
                break;
            case DECORATIONS:
                key = "decorations";
                break;
            case REDSTONE:
                key = "redstone";
                break;
            case TRANSPORTATION:
                key = "transportation";
                break;
            case MISC:
                key = "misc";
                break;
            case FOOD:
                key = "food";
                break;
            case TOOLS:
                key = "tools";
                break;
            case COMBAT:
                key = "combat";
                break;
            case BREWING:
                key = "brewing";
                break;
            default:
                throw new IllegalStateException("Unknown creative category: " + this);
        }
        return "itemGroup." + key;
    }
}
