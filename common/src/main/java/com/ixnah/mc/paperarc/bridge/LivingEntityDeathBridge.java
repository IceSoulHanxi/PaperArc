package com.ixnah.mc.paperarc.bridge;

import net.minecraft.world.item.ItemStack;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;

/**
 * Paper 的「可取消死亡事件」（{@code Improve-death-events.patch}）需要在 NMS 侧带的几段状态，
 * 由 {@code mixin.common.entity.LivingEntityDeathMixin} 注入。
 *
 * <ul>
 *   <li>{@code silentDeath}：Paper 同名字段。vanilla 在 {@code LivingEntity#hurt} 里就把死亡音效
 *       放了，Paper 把它挪进事件；这个标志记录"本来会不会响"，供
 *       {@code EntityDeathEvent#shouldPlayDeathSound()} 的初值用。</li>
 *   <li>{@code deathEvent}：{@code dropAllDeathLoot} 里 fire 出来的事件对象，
 *       在 {@code die} 里读回判断是否被取消（Arclight 的 {@code dropAllDeathLoot} 返回 void，
 *       没有 Paper 那条返回值通道）。</li>
 *   <li>{@code deathEquipment}：死亡前的装备快照。{@code dropEquipment} 在事件之前就把装备槽清了，
 *       取消死亡必须还原，否则"复活"等于扒光装备。</li>
 *   <li>{@code cancelledDrops} / {@code keptItems}：取消时要塞回玩家背包的掉落物、
 *       以及 {@code PlayerDeathEvent#getItemsToKeep()} 里要在重生后还回去的物品。</li>
 * </ul>
 */
public interface LivingEntityDeathBridge {

    boolean paperarc$isSilentDeath();

    void paperarc$setSilentDeath(boolean silentDeath);

    EntityDeathEvent paperarc$getDeathEvent();

    void paperarc$setDeathEvent(EntityDeathEvent event);

    ItemStack[] paperarc$getDeathEquipment();

    void paperarc$setDeathEquipment(ItemStack[] equipment);

    List<org.bukkit.inventory.ItemStack> paperarc$getCancelledDrops();

    void paperarc$setCancelledDrops(List<org.bukkit.inventory.ItemStack> drops);

    List<org.bukkit.inventory.ItemStack> paperarc$getKeptItems();

    void paperarc$setKeptItems(List<org.bukkit.inventory.ItemStack> items);
}
