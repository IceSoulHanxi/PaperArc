package com.ixnah.mc.paperarc.bridge;

import net.minecraft.sounds.SoundEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Paper 的 Improve-death-events 在 NMS {@code LivingEntity} 上加的状态，外加两个
 * 把 protected 成员开给别的类用的转发（避免为 {@code getDeathSound}/{@code getSoundVolume}
 * 加 AT —— 我们本来就 mixin 进 LivingEntity，用 {@code @Shadow} 更省事）。
 *
 * <p>{@code silentDeath}：Paper 把"死亡音效该不该响"从 {@code hurt} 里挪进了
 * {@code EntityDeathEvent}，于是需要一个标记把 {@code hurt} 里的 {@code flag1}
 * 传到事件构造处。
 *
 * <p>{@code pendingDeathEvent}：事件在 {@code dropAllDeathLoot} 深处（Arclight 走
 * Forge 的 {@code LivingDropsEvent}）触发，而"取消 → 复活"要在 {@code die} 里消费，
 * 中间隔着好几层调用，用实体上的字段传递。
 */
public interface DeathEventBridge {

    boolean paperarc$isSilentDeath();

    void paperarc$setSilentDeath(boolean silent);

    EntityDeathEvent paperarc$getPendingDeathEvent();

    void paperarc$setPendingDeathEvent(EntityDeathEvent event);

    SoundEvent paperarc$getDeathSound();

    float paperarc$getDeathSoundVolume();
}
