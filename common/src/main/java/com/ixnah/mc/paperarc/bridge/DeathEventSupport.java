package com.ixnah.mc.paperarc.bridge;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.CraftSound;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Paper 的 {@code CraftEventFactory#populateFields/playDeathSound}（Improve-death-events）。
 *
 * <p>Arclight 在三个地方造 {@code EntityDeathEvent}（{@code ArclightEventFactory}、
 * {@code CraftEventFactory}、盔甲架），所以 populate 直接挂在事件构造器上；
 * 播音则必须在事件**触发之后**，由各触发点调这里。
 */
public final class DeathEventSupport {

    private DeathEventSupport() {
    }

    /** 事件构造时填 Paper 新增的六个字段（声音一族 + reviveHealth）。 */
    public static void populate(EntityDeathEvent event, net.minecraft.world.entity.LivingEntity victim) {
        org.bukkit.entity.LivingEntity bukkit = event.getEntity();
        org.bukkit.attribute.AttributeInstance maxHealth =
                bukkit.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        event.setReviveHealth(maxHealth == null ? bukkit.getHealth() : maxHealth.getValue());

        DeathEventBridge bridge = (DeathEventBridge) victim;
        event.setShouldPlayDeathSound(!bridge.paperarc$isSilentDeath() && !victim.isSilent());
        SoundEvent sound = bridge.paperarc$getDeathSound();
        event.setDeathSound(sound == null ? null : CraftSound.getBukkit(sound));
        event.setDeathSoundCategory(org.bukkit.SoundCategory.valueOf(victim.getSoundSource().name()));
        event.setDeathSoundVolume(bridge.paperarc$getDeathSoundVolume());
        event.setDeathSoundPitch(victim.getVoicePitch());
    }

    /**
     * Paper 的 {@code ServerPlayer#processKeep}（PlayerDeathEvent#getItemsToKeep）：
     * 清空背包时把插件放进 {@code itemsToKeep} 的物品留下。
     *
     * <p>{@code inv == null} 表示"收尾"：{@code itemsToKeep} 里还剩的（插件自己加的、
     * 原本不在掉落里的）直接塞回背包。
     */
    public static void processKeep(org.bukkit.event.entity.PlayerDeathEvent event,
                                   net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> inv) {
        java.util.List<org.bukkit.inventory.ItemStack> itemsToKeep = event.getItemsToKeep();
        if (inv == null) {
            for (org.bukkit.inventory.ItemStack stack : itemsToKeep) {
                event.getEntity().getInventory().addItem(stack);
            }
            return;
        }
        for (int i = 0; i < inv.size(); i++) {
            net.minecraft.world.item.ItemStack item = inv.get(i);
            if (net.minecraft.world.item.enchantment.EnchantmentHelper.hasVanishingCurse(item)
                    || itemsToKeep.isEmpty() || item.isEmpty()) {
                inv.set(i, net.minecraft.world.item.ItemStack.EMPTY);
                continue;
            }
            org.bukkit.inventory.ItemStack bukkitStack =
                    org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(item);
            boolean keep = false;
            java.util.Iterator<org.bukkit.inventory.ItemStack> iterator = itemsToKeep.iterator();
            while (iterator.hasNext()) {
                if (bukkitStack.equals(iterator.next())) {
                    iterator.remove();
                    keep = true;
                    break;
                }
            }
            if (!keep) {
                inv.set(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }

    /** 事件未被取消时按事件里的值播死亡音效（vanilla 里那一句已被我们抑制）。 */
    public static void playDeathSound(net.minecraft.world.entity.LivingEntity victim, EntityDeathEvent event) {
        if (!event.shouldPlayDeathSound() || event.getDeathSound() == null
                || event.getDeathSoundCategory() == null) {
            return;
        }
        net.minecraft.world.entity.player.Player source =
                victim instanceof net.minecraft.world.entity.player.Player player ? player : null;
        Location at = event.getEntity().getLocation();
        SoundEvent sound = CraftSound.getSoundEffect(event.getDeathSound());
        SoundSource category = SoundSource.valueOf(event.getDeathSoundCategory().name());
        victim.level().playSound(source, at.getX(), at.getY(), at.getZ(), sound, category,
                event.getDeathSoundVolume(), event.getDeathSoundPitch());
    }
}
