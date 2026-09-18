package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.FireworkEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code FireworkEffect.Type} 实现 adventure {@code Translatable}（A6/X-2 第三批）。
 *
 * <p>paper 读的是它自己给枚举加的私有字段 {@code name}，运行时（spigot）那个枚举没有这个
 * 字段，所以这里把对应关系写死 —— 取值逐个从 paper-api 的 {@code <clinit>} 里 javap 出来
 * （BALL=small_ball / BALL_LARGE=large_ball / STAR=star / BURST=burst / CREEPER=creeper）。
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
                throw new IllegalStateException("Unknown firework shape: " + this);
        }
        return "item.minecraft.firework_star.shape." + shape;
    }
}
