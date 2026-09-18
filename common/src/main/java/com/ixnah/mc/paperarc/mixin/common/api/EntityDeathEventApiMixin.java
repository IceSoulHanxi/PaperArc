package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcDeathEvents;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.damage.DamageSource;
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
 * Paper 的 {@code Improve-death-events.patch}（API 侧）：让 {@code EntityDeathEvent}
 * 可取消，并把死亡音效的四个属性交给插件（gaps.md §3.1「死亡一族」14 条）。
 *
 * <p>初值全部在构造器 RETURN 处按 Paper 的 {@code CraftEventFactory#populateFields} 填，
 * 这样 Arclight 走哪条构造路径（{@code EntityDeathEvent} / {@code PlayerDeathEvent}
 * 的任一重载最终都汇到这个四参构造器）都能覆盖到，不用去 Arclight 的事件工厂里各补一遍。
 *
 * <p>消费方：
 * <ul>
 *   <li>音效 —— {@code entity.LivingEntityDeathMixin} 把 vanilla 在 {@code hurt} 里那次
 *       {@code makeSound(getDeathSound())} 掐掉，改由
 *       {@code server.ArclightEventFactoryDeathMixin} 在事件派发之后按这里的四个属性补放；</li>
 *   <li>取消 —— 同上两个 mixin：掉落/经验清空，{@code die} 在 {@code dropAllDeathLoot}
 *       之后读回事件，取消则还原血量与装备并跳过后半段。</li>
 * </ul>
 *
 * <p><b>已知限制</b>：{@code PlayerDeathEvent} 的取消目前不生效，原因见
 * {@code docs/gaps.md} §3.1（Arclight 用 {@code @Decorate} 接管了 {@code ServerPlayer#die}
 * 的后半段，那段代码在第三方注入器跑完之后才拼进目标，锚不住）。
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

    @Inject(method = "<init>(Lorg/bukkit/entity/LivingEntity;Lorg/bukkit/damage/DamageSource;Ljava/util/List;I)V",
            at = @At("RETURN"))
    private void paperarc$populateFields(LivingEntity entity, DamageSource damageSource,
                                         List<ItemStack> drops, int droppedExp, CallbackInfo ci) {
        this.paperarc$reviveHealth = PaperarcDeathEvents.maxHealth(entity);
        this.paperarc$shouldPlayDeathSound = PaperarcDeathEvents.shouldPlayDeathSound(entity);
        this.paperarc$deathSound = PaperarcDeathEvents.deathSound(entity);
        this.paperarc$deathSoundCategory = PaperarcDeathEvents.deathSoundCategory(entity);
        this.paperarc$deathSoundVolume = PaperarcDeathEvents.deathSoundVolume(entity);
        this.paperarc$deathSoundPitch = PaperarcDeathEvents.deathSoundPitch(entity);
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
    public void setReviveHealth(double reviveHealth) {
        double maxHealth = PaperarcDeathEvents.maxHealth(((EntityDeathEvent) (Object) this).getEntity());
        if ((maxHealth != 0 && reviveHealth <= 0) || (reviveHealth > maxHealth)) {
            throw new IllegalArgumentException("Health must be between 0 (exclusive) and " + maxHealth
                    + " (inclusive), but was " + reviveHealth);
        }
        this.paperarc$reviveHealth = reviveHealth;
    }

    @Unique
    public boolean shouldPlayDeathSound() {
        return this.paperarc$shouldPlayDeathSound;
    }

    @Unique
    public void setShouldPlayDeathSound(boolean playDeathSound) {
        this.paperarc$shouldPlayDeathSound = playDeathSound;
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
