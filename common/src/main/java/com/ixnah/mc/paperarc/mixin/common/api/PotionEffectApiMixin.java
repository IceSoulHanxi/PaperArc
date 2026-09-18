package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code PotionEffect} 不可变"改一项"系列（A6/X-2 第五批）。
 * 逐条照 paper：复制其余全部属性，只换一个。
 */
@Mixin(PotionEffect.class)
public abstract class PotionEffectApiMixin {

    @Unique
    private PotionEffect paperarc$self() {
        return (PotionEffect) (Object) this;
    }

    @Unique
    private PotionEffect paperarc$copy(PotionEffectType type, int duration, int amplifier,
                                       boolean ambient, boolean particles, boolean icon) {
        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
    }

    @Unique
    public PotionEffect withType(PotionEffectType type) {
        PotionEffect self = this.paperarc$self();
        return this.paperarc$copy(type, self.getDuration(), self.getAmplifier(),
                self.isAmbient(), self.hasParticles(), self.hasIcon());
    }

    @Unique
    public PotionEffect withDuration(int duration) {
        PotionEffect self = this.paperarc$self();
        return this.paperarc$copy(self.getType(), duration, self.getAmplifier(),
                self.isAmbient(), self.hasParticles(), self.hasIcon());
    }

    @Unique
    public PotionEffect withAmplifier(int amplifier) {
        PotionEffect self = this.paperarc$self();
        return this.paperarc$copy(self.getType(), self.getDuration(), amplifier,
                self.isAmbient(), self.hasParticles(), self.hasIcon());
    }

    @Unique
    public PotionEffect withAmbient(boolean ambient) {
        PotionEffect self = this.paperarc$self();
        return this.paperarc$copy(self.getType(), self.getDuration(), self.getAmplifier(),
                ambient, self.hasParticles(), self.hasIcon());
    }

    @Unique
    public PotionEffect withParticles(boolean particles) {
        PotionEffect self = this.paperarc$self();
        return this.paperarc$copy(self.getType(), self.getDuration(), self.getAmplifier(),
                self.isAmbient(), particles, self.hasIcon());
    }

    @Unique
    public PotionEffect withIcon(boolean icon) {
        PotionEffect self = this.paperarc$self();
        return this.paperarc$copy(self.getType(), self.getDuration(), self.getAmplifier(),
                self.isAmbient(), self.hasParticles(), icon);
    }
}
