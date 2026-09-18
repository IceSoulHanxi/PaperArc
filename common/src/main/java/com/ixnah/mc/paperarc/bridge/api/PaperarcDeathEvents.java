package com.ixnah.mc.paperarc.bridge.api;

import com.ixnah.mc.paperarc.bridge.LivingEntityDeathBridge;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v.CraftSound;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Paper {@code Improve-death-events.patch} 里 {@code CraftEventFactory} 的两个私有辅助
 * （{@code populateFields} / {@code playDeathSound}），加上 Arclight 架构下额外需要的
 * 「取消死亡」收尾。放在 bridge 里是因为 mixin 类不能用自身类型做签名、也不放实现体
 * （见 docs/mixin-conventions.md）。
 */
public final class PaperarcDeathEvents {

    private PaperarcDeathEvents() {
    }

    /** {@code populateFields} 的数值来源：Paper 用 GENERIC_MAX_HEALTH 属性当默认复活血量。 */
    public static double maxHealth(org.bukkit.entity.LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return attribute == null ? entity.getHealth() : attribute.getValue();
    }

    public static boolean shouldPlayDeathSound(org.bukkit.entity.LivingEntity entity) {
        net.minecraft.world.entity.LivingEntity handle = handle(entity);
        if (handle == null) {
            return false;
        }
        return !((LivingEntityDeathBridge) handle).paperarc$isSilentDeath() && !handle.isSilent();
    }

    public static Sound deathSound(org.bukkit.entity.LivingEntity entity) {
        net.minecraft.world.entity.LivingEntity handle = handle(entity);
        if (handle == null) {
            return null;
        }
        SoundEvent sound = handle.getDeathSound();
        return sound == null ? null : CraftSound.minecraftToBukkit(sound);
    }

    public static SoundCategory deathSoundCategory(org.bukkit.entity.LivingEntity entity) {
        net.minecraft.world.entity.LivingEntity handle = handle(entity);
        return handle == null ? null : SoundCategory.valueOf(handle.getSoundSource().name());
    }

    public static float deathSoundVolume(org.bukkit.entity.LivingEntity entity) {
        net.minecraft.world.entity.LivingEntity handle = handle(entity);
        return handle == null ? 1.0F : handle.getSoundVolume();
    }

    public static float deathSoundPitch(org.bukkit.entity.LivingEntity entity) {
        net.minecraft.world.entity.LivingEntity handle = handle(entity);
        return handle == null ? 1.0F : handle.getVoicePitch();
    }

    private static net.minecraft.world.entity.LivingEntity handle(org.bukkit.entity.LivingEntity entity) {
        // 插件自己 new 一个事件时 entity 可能不是 CraftLivingEntity，那就退回默认值而不是抛。
        return entity instanceof CraftLivingEntity craft ? craft.getHandle() : null;
    }

    /**
     * 事件派发完成之后、Arclight 把结果写回掉落/经验之前的收尾，对应 Paper 在
     * {@code CraftEventFactory#callEntityDeathEvent} 事件之后那一段。
     */
    public static void afterFired(net.minecraft.world.entity.LivingEntity victim, EntityDeathEvent event) {
        LivingEntityDeathBridge bridge = (LivingEntityDeathBridge) victim;
        bridge.paperarc$setDeathEvent(event);
        bridge.paperarc$setCancelledDrops(null);
        bridge.paperarc$setKeptItems(null);

        if (event.isCancelled()) {
            if (victim instanceof net.minecraft.server.level.ServerPlayer) {
                // 玩家死亡的后半段被 Arclight 的 @Decorate 接管，锚不住，取消收不回来。
                // 这里**不能**顺手清掉掉落 —— 那会变成"人照死、东西没了"，比不支持取消更坏。
                warnPlayerDeathCancel();
                bridge.paperarc$setDeathEvent(null);
            } else {
                // 取消：掉落与经验都不能落地。Arclight 后面用 event.getDrops() 回写 NMS 掉落列表
                // （EntityDropContainer#convert 先 clear 再按这份列表重建），清空即等于不掉。
                bridge.paperarc$setCancelledDrops(new ArrayList<>(event.getDrops()));
                event.getDrops().clear();
                event.setDroppedExp(0);
                return;
            }
        }

        if (event instanceof PlayerDeathEvent playerDeath) {
            if (!playerDeath.shouldDropExperience()) {
                event.setDroppedExp(0);
            }
            if (!playerDeath.getKeepInventory()) {
                bridge.paperarc$setKeptItems(processKeep(playerDeath));
            }
        }
        playDeathSound(victim, event);
        bridge.paperarc$setSilentDeath(false);
    }

    /**
     * Paper {@code ServerPlayer#processKeep} 的等价物。Paper 是"从背包里剔掉不保留的"，
     * Arclight 这边背包已经被 {@code Inventory#dropAll} 清空、物品都在掉落列表里，
     * 于是改成"从掉落列表里挑出要保留的，重生时再塞回去"——对插件可见的结果一致。
     */
    private static List<org.bukkit.inventory.ItemStack> processKeep(PlayerDeathEvent event) {
        List<org.bukkit.inventory.ItemStack> toKeep = event.getItemsToKeep();
        if (toKeep.isEmpty()) {
            return null;
        }
        List<org.bukkit.inventory.ItemStack> kept = new ArrayList<>(toKeep);
        for (org.bukkit.inventory.ItemStack wanted : kept) {
            Iterator<org.bukkit.inventory.ItemStack> drops = event.getDrops().iterator();
            while (drops.hasNext()) {
                if (drops.next().equals(wanted)) {
                    drops.remove();
                    break;
                }
            }
        }
        return kept;
    }

    private static volatile boolean warnedPlayerDeathCancel;

    private static void warnPlayerDeathCancel() {
        if (warnedPlayerDeathCancel) {
            return;
        }
        warnedPlayerDeathCancel = true;
        com.ixnah.mc.paperarc.PaperArcPlatform.logger().warning(
                "PlayerDeathEvent#setCancelled(true) 在 Arclight 上无效：ServerPlayer#die 的后半段由 Arclight 的 "
                        + "@Decorate 接管，注入锚不住（见 docs/gaps.md §3.1 死亡一族）。本次取消被忽略，"
                        + "掉落与经验按未取消处理。");
    }

    /** Paper {@code CraftEventFactory#playDeathSound}：音效从触发点挪进事件之后，才允许插件改。 */
    public static void playDeathSound(net.minecraft.world.entity.LivingEntity victim, EntityDeathEvent event) {
        if (!event.shouldPlayDeathSound() || event.getDeathSound() == null
                || event.getDeathSoundCategory() == null) {
            return;
        }
        net.minecraft.world.entity.player.Player source =
                victim instanceof net.minecraft.world.entity.player.Player player ? player : null;
        SoundEvent sound = CraftSound.bukkitToMinecraft(event.getDeathSound());
        if (sound == null) {
            return;
        }
        victim.level().playSound(source, victim.getX(), victim.getY(), victim.getZ(), sound,
                net.minecraft.sounds.SoundSource.valueOf(event.getDeathSoundCategory().name()),
                event.getDeathSoundVolume(), event.getDeathSoundPitch());
    }

    /**
     * {@code PlayerDeathEvent#getItemsToKeep()} 的兑现点：重生时把上一具身体上记下的
     * 保留物品塞进新背包。Arclight 在 {@code ServerPlayerMixin#arclight$restoreFromDeath}
     * 里只处理"整份背包保留"（keepInventory），没有逐件保留这条路。
     */
    public static void restoreKeptItems(net.minecraft.server.level.ServerPlayer newPlayer,
                                        net.minecraft.server.level.ServerPlayer oldPlayer) {
        LivingEntityDeathBridge bridge = (LivingEntityDeathBridge) oldPlayer;
        List<org.bukkit.inventory.ItemStack> kept = bridge.paperarc$getKeptItems();
        bridge.paperarc$setKeptItems(null);
        if (kept == null || kept.isEmpty()) {
            return;
        }
        org.bukkit.entity.Entity bukkit = com.ixnah.mc.paperarc.bridge.PaperArcBridge.bukkitEntity(newPlayer);
        if (bukkit instanceof org.bukkit.entity.Player player) {
            for (org.bukkit.inventory.ItemStack stack : kept) {
                if (stack != null && stack.getType() != org.bukkit.Material.AIR) {
                    player.getInventory().addItem(stack);
                }
            }
        }
    }

    /** {@code die} 开头对装备的快照：{@code dropEquipment} 会在事件之前清空装备槽。 */
    public static ItemStack[] snapshotEquipment(net.minecraft.world.entity.LivingEntity victim) {
        EquipmentSlot[] slots = EquipmentSlot.values();
        ItemStack[] snapshot = new ItemStack[slots.length];
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = victim.getItemBySlot(slots[i]);
            snapshot[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        }
        return snapshot;
    }

    /**
     * 取消死亡的收尾：血量回到 {@code getReviveHealth()}、装备与（玩家的）背包还原。
     * 返回 true 表示确实取消了，调用方要跳过 {@code die} 的后半段。
     */
    public static boolean revertDeath(net.minecraft.world.entity.LivingEntity victim, DamageSource source) {
        LivingEntityDeathBridge bridge = (LivingEntityDeathBridge) victim;
        EntityDeathEvent event = bridge.paperarc$getDeathEvent();
        bridge.paperarc$setDeathEvent(null);
        if (event == null || !event.isCancelled()) {
            bridge.paperarc$setDeathEquipment(null);
            bridge.paperarc$setCancelledDrops(null);
            return false;
        }

        ItemStack[] snapshot = bridge.paperarc$getDeathEquipment();
        bridge.paperarc$setDeathEquipment(null);
        if (snapshot != null) {
            EquipmentSlot[] slots = EquipmentSlot.values();
            for (int i = 0; i < slots.length && i < snapshot.length; i++) {
                if (snapshot[i] != null && !snapshot[i].isEmpty()) {
                    victim.setItemSlot(slots[i], snapshot[i]);
                }
            }
        }

        List<org.bukkit.inventory.ItemStack> drops = bridge.paperarc$getCancelledDrops();
        bridge.paperarc$setCancelledDrops(null);
        if (drops != null && !drops.isEmpty() && event.getEntity() instanceof org.bukkit.entity.Player player) {
            // 玩家的背包在 dropAllDeathLoot 里已经被 dropAll() 清空，掉落列表就是那份背包。
            for (org.bukkit.inventory.ItemStack stack : drops) {
                if (stack != null && stack.getType() != org.bukkit.Material.AIR) {
                    player.getInventory().addItem(stack);
                }
            }
        }

        victim.setHealth((float) event.getReviveHealth());
        return true;
    }
}
