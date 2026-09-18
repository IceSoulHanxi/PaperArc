package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.FireworkEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code FireworkEffect.Type} 实现 adventure 的 {@code Translatable}
 * （checklist §1.12 bb）。
 *
 * <p>paper 的实现读的是它自己给每个常量加的私有 {@code name} 字段
 * （{@code "item.minecraft.firework_star.shape." + name}），运行时枚举没有这个字段，
 * 只能按常量逐条映射。映射表来自 paper 的 {@code <clinit>}（javap -c）：
 * BALL→small_ball、BALL_LARGE→large_ball、STAR→star、BURST→burst、CREEPER→creeper。
 */
@Mixin(FireworkEffect.Type.class)
public abstract class FireworkEffectTypeApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        String shape;
        switch ((FireworkEffect.Type) (Object) this) {
            case BALL:
                shape = "small_ball";
                break;
            case BALL_LARGE:
                shape = "large_ball";
                break;
            case STAR:
                shape = "star";
                break;
            case BURST:
                shape = "burst";
                break;
            case CREEPER:
                shape = "creeper";
                break;
            default:
                shape = ((FireworkEffect.Type) (Object) this).name().toLowerCase(java.util.Locale.ENGLISH);
                break;
        }
        return "item.minecraft.firework_star.shape." + shape;
    }
}
