package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code PotionEffect} 上的 6 个 {@code withXxx()}（checklist §1.10 am，第 ⑤ 批）。
 *
 * <p>paper 的实现读私有字段再 new，这里用运行时已有的公开 getter，结果完全一致。
 * 没补 {@code getHiddenPotionEffect()}：那是 paper 自己在 PotionEffect 上加的字段，
 * 运行时不存在也没人往里写，返回 null 会让插件以为"没有被覆盖的效果"从而给出错误结论，
 * 见 docs/gaps.md。</p>
 */
@Mixin(PotionEffect.class)
public abstract class PotionEffectApiMixin {

    @Unique
    private PotionEffect paperarc$self() {
        return (PotionEffect) (Object) this;
    }

    @Unique
    public PotionEffect withType(PotionEffectType type) {
        PotionEffect self = this.paperarc$self();
        return new PotionEffect(type, self.getDuration(), self.getAmplifier(),
                self.isAmbient(), self.hasParticles(), self.hasIcon());
    }

    @Unique
    public PotionEffect withDuration(int duration) {
        PotionEffect self = this.paperarc$self();
        return new PotionEffect(self.getType(), duration, self.getAmplifier(),
                self.isAmbient(), self.hasParticles(), self.hasIcon());
    }

    @Unique
    public PotionEffect withAmplifier(int amplifier) {
        PotionEffect self = this.paperarc$self();
        return new PotionEffect(self.getType(), self.getDuration(), amplifier,
                self.isAmbient(), self.hasParticles(), self.hasIcon());
    }

    @Unique
    public PotionEffect withAmbient(boolean ambient) {
        PotionEffect self = this.paperarc$self();
        return new PotionEffect(self.getType(), self.getDuration(), self.getAmplifier(),
                ambient, self.hasParticles(), self.hasIcon());
    }

    @Unique
    public PotionEffect withParticles(boolean particles) {
        PotionEffect self = this.paperarc$self();
        return new PotionEffect(self.getType(), self.getDuration(), self.getAmplifier(),
                self.isAmbient(), particles, self.hasIcon());
    }

    @Unique
    public PotionEffect withIcon(boolean icon) {
        PotionEffect self = this.paperarc$self();
        return new PotionEffect(self.getType(), self.getDuration(), self.getAmplifier(),
                self.isAmbient(), self.hasParticles(), icon);
    }
}
