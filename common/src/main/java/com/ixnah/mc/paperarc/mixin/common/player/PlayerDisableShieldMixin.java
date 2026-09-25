package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's Add-PlayerShieldDisableEvent patch.
 *
 * In 1.21.5+, shield disabling is handled via {@link BlocksAttacks#disable}.
 * When {@link Player#blockUsingItem(ServerLevel, LivingEntity)} invokes {@code disable},
 * we fire {@link PlayerShieldDisableEvent}.
 * Cancellation prevents the disable entirely.
 * If the cooldown was modified by the event, we recalculate the damage parameter
 * so vanilla's {@code disable} applies the exact modified cooldown ticks and sound.
 */
@Mixin(Player.class)
public abstract class PlayerDisableShieldMixin {

    @WrapOperation(
        method = "blockUsingItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/component/BlocksAttacks;disable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/item/ItemStack;)V"
        )
    )
    private void paperarc$onDisableShield(
        BlocksAttacks instance,
        ServerLevel level,
        LivingEntity entity,
        float seconds,
        ItemStack stack,
        Operation<Void> original,
        @Local(argsOnly = true) LivingEntity attacker
    ) {
        float scale = instance.disableCooldownScale();
        int defaultCooldown = Math.max(0, Math.round(seconds * scale * 20.0f));
        Player self = (Player) (Object) this;
        PlayerShieldDisableEvent event = new PlayerShieldDisableEvent(
            PaperArcBridge.bukkitPlayer(self),
            attacker != null ? PaperArcBridge.bukkitEntity(attacker) : null,
            defaultCooldown
        );
        PaperArcBridge.fire(event);
        if (event.isCancelled()) {
            return;
        }
        int newCooldown = event.getCooldown();
        if (newCooldown <= 0) {
            return;
        }
        float newDamage = scale > 0.0f ? ((float) newCooldown) / (scale * 20.0f) : seconds;
        original.call(instance, level, entity, newDamage, stack);
    }
}
