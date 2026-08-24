package dev.paperarc.mixin.common.player;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

/**
 * Port of Paper's PrePlayerAttackEntityEvent.
 *
 * Paper (1.21.1, Add-PrePlayerAttackEntityEvent.patch) rewrites
 * {@code Player.attack}:
 *   if (target.isAttackable()) {
 *       if (!target.skipAttackInteraction(this)) { ...attack... }
 *   }
 * into
 *   willAttack = target.isAttackable() && !target.skipAttackInteraction(this);
 *   event = new PrePlayerAttackEntityEvent(player, target, willAttack);
 *   if (event.callEvent() && willAttack) { ...attack... }
 *
 * We wrap the skipAttackInteraction INVOKE instead: the event fires with
 * willAttack = original return value; returning false skips the whole attack
 * block exactly like Paper.
 *
 * Note: when target.isAttackable() is false vanilla never reaches
 * skipAttackInteraction, so the event does not fire in that case either
 * (same observable behavior as Paper, where isAttackable feeds willAttack).
 */
@Mixin(Player.class)
public abstract class PlayerAttackPreMixin {

    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;skipAttackInteraction(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean paperarc$preAttack(Entity target, Entity attacker, Operation<Boolean> original) {
        boolean willAttack = original.call(target, attacker);
        PrePlayerAttackEntityEvent event = new PrePlayerAttackEntityEvent(
                PaperArcBridge.bukkitPlayer((Player) attacker),
                PaperArcBridge.bukkitEntity(target),
                willAttack
        );
        return willAttack && event.callEvent();
    }
}
