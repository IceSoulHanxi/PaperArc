package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.api.PaperarcDeathEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Paper 在 {@code CraftEventFactory#callEntityDeathEvent} / {@code callPlayerDeathEvent}
 * 里派发事件之后做的那一段（{@code playDeathSound} + 取消时提前返回），搬到 Arclight 的
 * 对应位置。
 *
 * <p>Arclight 不走 CraftBukkit 的那两个方法：死亡事件由
 * {@code EntityEventHandler#monitorLivingDrops}（LivingDropsEvent 的订阅者）调
 * {@code ArclightEventFactory#callEntityDeathEvent / callPlayerDeathEvent} 派发，
 * 回来之后才把 {@code event.getDrops()} 写回 NMS 掉落列表、把 {@code getDroppedExp()}
 * 写进 {@code expToDrop}。所以「事件之后、写回之前」这个位置就是这两个方法里那一次
 * {@code callEvent(...)} 的返回点。
 *
 * <p>编译期靠 {@code src/arclightStub} 里的同名桩让注解处理器能解析；运行时用的是
 * Arclight 自己那份（同 {@code EntityClassLookupMixin}）。
 */
@Mixin(targets = "io.izzel.arclight.common.mod.server.event.ArclightEventFactory", remap = false)
public abstract class ArclightEventFactoryDeathMixin {

    @WrapOperation(method = "callEntityDeathEvent", remap = false,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lio/izzel/arclight/common/mod/server/event/ArclightEventFactory;callEvent(Lorg/bukkit/event/Event;)Lorg/bukkit/event/Event;"))
    private static Event paperarc$afterEntityDeathEvent(Event event, Operation<Event> original,
                                                        @Local(argsOnly = true) LivingEntity victim) {
        Event fired = original.call(event);
        if (fired instanceof EntityDeathEvent death) {
            PaperarcDeathEvents.afterFired(victim, death);
        }
        return fired;
    }

    @WrapOperation(method = "callPlayerDeathEvent", remap = false,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lio/izzel/arclight/common/mod/server/event/ArclightEventFactory;callEvent(Lorg/bukkit/event/Event;)Lorg/bukkit/event/Event;"))
    private static Event paperarc$afterPlayerDeathEvent(Event event, Operation<Event> original,
                                                        @Local(argsOnly = true) ServerPlayer victim) {
        Event fired = original.call(event);
        if (fired instanceof EntityDeathEvent death) {
            PaperarcDeathEvents.afterFired(victim, death);
        }
        return fired;
    }
}
