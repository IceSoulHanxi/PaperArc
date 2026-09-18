package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code EntityUnleashEvent} 可取消 + {@code isDropLeash()/setDropLeash(boolean)}
 * （Make-EntityUnleashEvent-cancellable.patch + Add-dropLeash-variable-to-EntityUnleashEvent.patch）。
 *
 * <p>两条都有真正的消费方：{@code MobUnleashMixin} / {@code PathfinderMobUnleashMixin} /
 * {@code LeashFenceKnotUnleashMixin} 把 {@code dropLeash(sendPacket, dropLead)} 的第二个实参
 * 换成 {@code isDropLeash()}，取消则整句不执行。
 *
 * <p>{@code dropLeash} 的默认值按 paper 各调用点的字面量给：多数是 {@code true}，
 * 换维度与玩家解绳两处在触发点 HEAD 另行压值（见 {@link EventCauseState#setUnleashDropLeash}）。
 *
 * <p>子类 {@code PlayerUnleashEntityEvent} 运行时**自带** {@code cancelled} 字段与
 * {@code isCancelled/setCancelled}，会覆盖这里的实现，Arclight 原有的取消处理不受影响。
 */
@Mixin(EntityUnleashEvent.class)
public abstract class EntityUnleashEventApiMixin implements Cancellable {

    @Unique
    private boolean paperarc$cancelled;

    @Unique
    private boolean paperarc$dropLeash;

    @Inject(method = "<init>(Lorg/bukkit/entity/Entity;Lorg/bukkit/event/entity/EntityUnleashEvent$UnleashReason;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$register(Entity entity, EntityUnleashEvent.UnleashReason reason, CallbackInfo ci) {
        this.paperarc$cancelled = false;
        this.paperarc$dropLeash = EventCauseState.takeUnleashDropLeash();
        EventCauseState.setLastUnleashEvent((EntityUnleashEvent) (Object) this);
    }

    @Unique
    public boolean isDropLeash() {
        return this.paperarc$dropLeash;
    }

    @Unique
    public void setDropLeash(boolean dropLeash) {
        this.paperarc$dropLeash = dropLeash;
    }

    @Unique
    public boolean isCancelled() {
        return this.paperarc$cancelled;
    }

    @Unique
    public void setCancelled(boolean cancel) {
        this.paperarc$cancelled = cancel;
    }
}
