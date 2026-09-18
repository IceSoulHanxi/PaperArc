package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.DeathEventSupport;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Paper 的 Improve-death-events / getItemsToKeep / shouldDropExperience 在玩家死亡路径上的三处消费点。
 *
 * <p>三个锚点都在 Arclight {@code @Overwrite} 之后的 {@code ServerPlayer#die} 体里
 * （Arclight `mixinPriority: 500` < 我们默认 1000，见任务书 Y-1）；
 * {@code callPlayerDeathEvent} / {@code dropExperience} / {@code Inventory.clearContent}
 * 在该方法体内各只出现一次（`javap -c` 核对 Arclight 编译产物 {@code ServerPlayerMixin#die}），
 * 局部 {@code PlayerDeathEvent event} 是 slot 9 且类型唯一，故 {@code @Local} 隐式匹配。
 *
 * <p>掉落物与经验字段的写入在 {@code CraftEventFactory#callPlayerDeathEvent} 里，
 * 取消要在那里就返回（见 {@code CraftEventFactoryDeathMixin}），这里只负责回血与中止后续流程。
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDeathEventMixin {

    @WrapOperation(
            method = "die",
            at = @At(value = "INVOKE", remap = false,
                    target = "Lorg/bukkit/craftbukkit/v/event/CraftEventFactory;callPlayerDeathEvent(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/List;Ljava/lang/String;Z)Lorg/bukkit/event/entity/PlayerDeathEvent;"))
    private PlayerDeathEvent paperarc$cancellablePlayerDeath(
            ServerPlayer victim, List<org.bukkit.inventory.ItemStack> loot, String deathMessage,
            boolean keepInventory, Operation<PlayerDeathEvent> original, @Cancellable CallbackInfo ci) {
        PlayerDeathEvent event = original.call(victim, loot, deathMessage, keepInventory);
        if (event.isCancelled()) {
            // 插件可能已经在监听器里把血加回去了，只在真的还是 0 时才补
            if (victim.getHealth() <= 0.0F) {
                victim.setHealth((float) event.getReviveHealth());
            }
            ci.cancel();
        }
        return event;
    }

    @WrapWithCondition(
            method = "die",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;dropExperience()V"))
    private boolean paperarc$dropExperience(ServerPlayer self, @Local PlayerDeathEvent event) {
        return event.shouldDropExperience();
    }

    @WrapOperation(
            method = "die",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;clearContent()V"))
    private void paperarc$processKeep(Inventory inventory, Operation<Void> original,
                                      @Local PlayerDeathEvent event) {
        if (event.getItemsToKeep().isEmpty()) {
            original.call(inventory);
            return;
        }
        DeathEventSupport.processKeep(event, inventory.items);
        DeathEventSupport.processKeep(event, inventory.armor);
        DeathEventSupport.processKeep(event, inventory.offhand);
        DeathEventSupport.processKeep(event, null);
    }
}
