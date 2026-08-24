package dev.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.CreeperIgniteEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's CreeperIgniteEvent (Add More Creeper API).
 * Paper rewrites {@code Creeper.ignite()} into {@code setIgnited(true)} which
 * fires the event; here we wrap the single {@code SynchedEntityData.set} call
 * inside {@code ignite()} instead, and replicate Paper's NBT-load bypass by
 * suppressing the event when {@code readAdditionalSaveData} calls ignite().
 */
@Mixin(Creeper.class)
public abstract class CreeperIgniteMixin {

    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_IS_IGNITED;

    @Shadow
    public abstract boolean isIgnited();

    @Unique
    private boolean paperarc$suppressIgniteEvent;

    @WrapOperation(
            method = "ignite",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V")
    )
    private void paperarc$ignite(SynchedEntityData instance, EntityDataAccessor<Boolean> accessor, Object value,
                                 Operation<Void> original) {
        boolean ignited = (Boolean) value;
        if (paperarc$suppressIgniteEvent || this.isIgnited() == ignited) {
            original.call(instance, accessor, value);
            return;
        }
        CreeperIgniteEvent event = new CreeperIgniteEvent(
                (org.bukkit.entity.Creeper) PaperArcBridge.bukkitEntity((Creeper) (Object) this), ignited);
        if (event.callEvent()) {
            original.call(instance, DATA_IS_IGNITED, event.isIgnited());
        }
    }

    /**
     * Paper sets the data directly on NBT load to avoid firing the event
     * while chunks load; we keep vanilla's {@code ignite()} call but suppress
     * the event around it.
     */
    @WrapOperation(
            method = "readAdditionalSaveData",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;ignite()V")
    )
    private void paperarc$nbtIgnite(Creeper instance, Operation<Void> original) {
        paperarc$suppressIgniteEvent = true;
        try {
            original.call(instance);
        } finally {
            paperarc$suppressIgniteEvent = false;
        }
    }
}
