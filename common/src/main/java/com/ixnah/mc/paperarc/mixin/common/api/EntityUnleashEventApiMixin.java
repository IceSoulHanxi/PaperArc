package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
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
 * <p>两条都有真正的消费方：{@code entity.LeashableUnleashMixin} 把
 * {@code Leashable#dropLeash(E,Z,Z)} 里的三件事（清 leashData、掉绳子、广播解绑包）
 * 按事件的取消标志与 {@code isDropLeash()} 逐个守住。
 *
 * <p>子类 {@code PlayerUnleashEntityEvent} 运行时自带 {@code cancelled} 与
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
        this.paperarc$dropLeash = PaperarcEventCauses.takeUnleashDropLeash();
        PaperarcEventCauses.rememberUnleashEvent((EntityUnleashEvent) (Object) this);
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
