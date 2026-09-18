package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.DeathEventSupport;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Paper 的 Improve-death-events：让 {@code EntityDeathEvent} 可取消，并暴露死亡音效一族
 * （checklist §1.12 bb 里"EntityDeathEvent 缺 Cancellable"那条，14 个方法）。
 *
 * <p>字段在**构造器**里填而不是像 Paper 那样在 {@code CraftEventFactory} 里填 ——
 * Arclight 造这个事件有三个入口（{@code ArclightEventFactory#callEntityDeathEvent}、
 * CraftBukkit 的 {@code CraftEventFactory}、盔甲架），挂构造器一处覆盖全部。
 *
 * <p>消费方（不做假实现的那一半）：
 * <ul>
 *   <li>取消 → {@code LivingEntityDeathEventMixin} 在 {@code die} 里把 {@code dead}
 *       置回 false、血量设成 {@code reviveHealth}，并跳过掉落与经验；</li>
 *   <li>死亡音效 → vanilla 在 {@code hurt} 里那一句 {@code playSound} 已被抑制，
 *       改由触发点在事件之后按事件里的值播（{@link DeathEventSupport#playDeathSound}）。</li>
 * </ul>
 */
@Mixin(EntityDeathEvent.class)
public abstract class EntityDeathEventApiMixin implements Cancellable {

    @Unique
    private boolean paperarc$cancelled;

    @Unique
    private double paperarc$reviveHealth;

    @Unique
    private boolean paperarc$shouldPlayDeathSound;

    @Unique
    private Sound paperarc$deathSound;

    @Unique
    private SoundCategory paperarc$deathSoundCategory;

    @Unique
    private float paperarc$deathSoundVolume;

    @Unique
    private float paperarc$deathSoundPitch;

    @Inject(method = "<init>(Lorg/bukkit/entity/LivingEntity;Ljava/util/List;I)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$populate(LivingEntity what, List<ItemStack> drops, int droppedExp, CallbackInfo ci) {
        this.paperarc$cancelled = false;
        if (what instanceof CraftLivingEntity craft) {
            DeathEventSupport.populate((EntityDeathEvent) (Object) this, craft.getHandle());
        }
    }

    @Unique
    public boolean isCancelled() {
        return this.paperarc$cancelled;
    }

    @Unique
    public void setCancelled(boolean cancel) {
        this.paperarc$cancelled = cancel;
    }

    @Unique
    public double getReviveHealth() {
        return this.paperarc$reviveHealth;
    }

    @Unique
    public void setReviveHealth(double reviveHealth) throws IllegalArgumentException {
        org.bukkit.attribute.AttributeInstance attribute = ((EntityDeathEvent) (Object) this).getEntity()
                .getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = attribute == null ? 0.0D : attribute.getValue();
        if ((maxHealth != 0.0D && reviveHealth <= 0.0D) || reviveHealth > maxHealth) {
            throw new IllegalArgumentException("Health must be between 0 (exclusive) and " + maxHealth
                    + " (inclusive), but was: " + reviveHealth);
        }
        this.paperarc$reviveHealth = reviveHealth;
    }

    @Unique
    public boolean shouldPlayDeathSound() {
        return this.paperarc$shouldPlayDeathSound;
    }

    @Unique
    public void setShouldPlayDeathSound(boolean shouldPlayDeathSound) {
        this.paperarc$shouldPlayDeathSound = shouldPlayDeathSound;
    }

    @Unique
    public Sound getDeathSound() {
        return this.paperarc$deathSound;
    }

    @Unique
    public void setDeathSound(Sound sound) {
        this.paperarc$deathSound = sound;
    }

    @Unique
    public SoundCategory getDeathSoundCategory() {
        return this.paperarc$deathSoundCategory;
    }

    @Unique
    public void setDeathSoundCategory(SoundCategory soundCategory) {
        this.paperarc$deathSoundCategory = soundCategory;
    }

    @Unique
    public float getDeathSoundVolume() {
        return this.paperarc$deathSoundVolume;
    }

    @Unique
    public void setDeathSoundVolume(float volume) {
        this.paperarc$deathSoundVolume = volume;
    }

    @Unique
    public float getDeathSoundPitch() {
        return this.paperarc$deathSoundPitch;
    }

    @Unique
    public void setDeathSoundPitch(float pitch) {
        this.paperarc$deathSoundPitch = pitch;
    }
}
