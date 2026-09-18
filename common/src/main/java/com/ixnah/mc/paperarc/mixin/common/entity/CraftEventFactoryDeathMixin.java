package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.DeathEventSupport;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Paper 在 {@code CraftEventFactory#callPlayerDeathEvent} 里加的两句：
 * 事件被取消就立刻返回（于是 keepLevel/newLevel/expToDrop 等字段不写、掉落物不生成），
 * 否则按事件里的死亡音效字段播音（vanilla 里 {@code hurt} 中那一句已被
 * {@code LivingEntityDeathEventMixin} 抑制）。
 *
 * <p>注入点在 {@code PluginManager#callEvent} 之后：该指令返回 void 且参数已消费，
 * 栈是空的，可以用 {@code @Inject}。局部 {@code PlayerDeathEvent}（slot 5）类型唯一。
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.event.CraftEventFactory", remap = false)
public abstract class CraftEventFactoryDeathMixin {

    @Inject(
            method = "callPlayerDeathEvent(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/List;Ljava/lang/String;Z)Lorg/bukkit/event/entity/PlayerDeathEvent;",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, remap = false,
                    target = "Lorg/bukkit/plugin/PluginManager;callEvent(Lorg/bukkit/event/Event;)V"),
            cancellable = true, remap = false)
    private static void paperarc$playerDeathEventTail(ServerPlayer victim,
                                                      java.util.List<org.bukkit.inventory.ItemStack> drops,
                                                      String deathMessage, boolean keepInventory,
                                                      CallbackInfoReturnable<PlayerDeathEvent> cir,
                                                      @Local PlayerDeathEvent event) {
        if (event.isCancelled()) {
            cir.setReturnValue(event);
            return;
        }
        DeathEventSupport.playDeathSound(victim, event);
    }
}
