package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/**
 * paper 加在 {@code GameMode} 枚举上的 2 个方法（checklist §1.10 am，第 ⑤ 批）。
 * paper 在构造器里算好存字段（{@code "gameMode." + name().toLowerCase(ENGLISH)}），
 * mixin 不改构造器，直接现算，取值完全一致。
 */
@Mixin(GameMode.class)
public abstract class GameModeApiMixin {

    @Unique
    private GameMode paperarc$self() {
        return (GameMode) (Object) this;
    }

    @Unique
    public String translationKey() {
        return "gameMode." + this.paperarc$self().name().toLowerCase(Locale.ENGLISH);
    }

    @Unique
    public boolean isInvulnerable() {
        GameMode self = this.paperarc$self();
        return self == GameMode.CREATIVE || self == GameMode.SPECTATOR;
    }
}
