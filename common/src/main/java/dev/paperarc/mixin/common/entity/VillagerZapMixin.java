package dev.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EntityZapEvent;
import com.llamalad7.mixinextras.sugar.Local;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.Villager;
import org.bukkit.entity.LightningStrike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's Add-EntityZapEvent.patch.
 *
 * <p>Paper fires {@link EntityZapEvent} in
 * {@code Villager#thunderHit(ServerLevel, LightningBolt)} right after the
 * replacement {@link Witch} is created and before it is positioned/spawned;
 * cancelling skips the whole conversion. We inject at the first use of the
 * created witch ({@code Witch.moveTo}) and capture it with a MixinExtras
 * {@code @Local}. Arclight's VillagerMixin injects thunderHit only at the
 * later {@code addFreshEntityWithPassengers} site (EntityTransformEvent), so
 * both handlers coexist: our event runs first, matching Paper ordering.
 */
@Mixin(Villager.class)
public abstract class VillagerZapMixin {

    @Inject(
            method = "thunderHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Witch;moveTo(DDDFF)V"
            ),
            cancellable = true
    )
    private void paperarc$fireZapEvent(ServerLevel level, LightningBolt lightning, CallbackInfo ci,
                                       @Local Witch witch) {
        var event = new EntityZapEvent(
                PaperArcBridge.bukkitEntity((Villager) (Object) this),
                (LightningStrike) PaperArcBridge.bukkitEntity(lightning),
                PaperArcBridge.bukkitEntity(witch));
        if (!event.callEvent()) {
            ci.cancel();
        }
    }
}
