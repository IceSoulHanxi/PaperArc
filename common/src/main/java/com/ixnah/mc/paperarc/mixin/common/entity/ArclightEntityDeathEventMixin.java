package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.DeathEventBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * 把 Arclight 触发 {@code EntityDeathEvent} 的那一处接上 Paper 的取消语义。
 *
 * <p>Arclight 不在 {@code dropAllDeathLoot} 里直接调 CraftBukkit 的
 * {@code CraftEventFactory.callEntityDeathEvent}，而是走 Forge 的 {@code LivingDropsEvent}
 * （{@code EntityEventDispatcher#onLivingDeath} → {@code ArclightEventFactory#callEntityDeathEvent}）。
 * 事件对象在那里造出来又立刻丢掉，所以这里把它挂到 NMS 实体上，
 * 交给 {@code LivingEntityDeathEventMixin} 在 {@code die} 里消费。
 *
 * <p>取消时清空 drops 列表：它是 Forge 掉落集合的 {@code XmapList} 视图，
 * 清空后 dispatcher 里那句 {@code if (drops.isEmpty()) event.setCanceled(true)}
 * 会取消 Forge 事件，掉落物就不会生成；经验则靠把 {@code droppedExp} 归零
 * （紧随其后的 {@code bridge$setExpToDrop(event.getDroppedExp())} 会读到 0）。
 */
@Mixin(targets = "io.izzel.arclight.common.mod.server.event.ArclightEventFactory", remap = false)
public abstract class ArclightEntityDeathEventMixin {

    @WrapOperation(
            method = "callEntityDeathEvent(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/List;)V",
            at = @At(value = "INVOKE", remap = false,
                    target = "Lio/izzel/arclight/common/mod/server/event/ArclightEventFactory;callEvent(Lorg/bukkit/event/Event;)V"),
            remap = false)
    private static void paperarc$stashDeathEvent(Event event, Operation<Void> original,
                                                 @Local(argsOnly = true, index = 0) LivingEntity victim,
                                                 @Local(argsOnly = true, index = 1) List<ItemStack> drops) {
        original.call(event);
        if (!(event instanceof EntityDeathEvent death)) {
            return;
        }
        ((DeathEventBridge) victim).paperarc$setPendingDeathEvent(death);
        if (death.isCancelled()) {
            drops.clear();
            death.setDroppedExp(0);
        }
    }
}
