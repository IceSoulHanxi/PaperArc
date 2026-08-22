package dev.paperarc.mixin.common.player;

import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's Add-PlayerShieldDisableEvent patch.
 *
 * Vanilla {@code Player#disableShield()} carries no attacker argument, but its
 * only caller {@code blockUsingShield(LivingEntity)} knows it. A capture
 * handler right before the {@code disableShield()} invoke stashes the attacker
 * in a ThreadLocal; a cancellable HEAD inject on {@code disableShield()} then
 * fires {@link PlayerShieldDisableEvent} and re-implements the body with
 * {@code event.getCooldown()}.
 *
 * Cancellation skips cooldown AND {@code stopUsingItem()} AND the entity-30
 * broadcast, mirroring Paper's early-return semantics. No captured attacker ->
 * plain vanilla path with no event (matches Paper's null-attacker branch).
 * Arclight's PlayerMixin has no decorations on disableShield/blockUsingShield.
 */
@Mixin(Player.class)
public abstract class PlayerDisableShieldMixin {

    @Shadow public abstract ItemCooldowns getCooldowns();

    @Unique
    private static final ThreadLocal<LivingEntity> paperarc$shieldAttacker = new ThreadLocal<>();

    @Inject(
        method = "blockUsingShield",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;disableShield()V")
    )
    private void paperarc$captureAttacker(LivingEntity attacker, CallbackInfo ci) {
        paperarc$shieldAttacker.set(attacker);
    }

    @Inject(method = "disableShield()V", at = @At("HEAD"), cancellable = true)
    private void paperarc$fireShieldDisable(CallbackInfo ci) {
        LivingEntity attacker = paperarc$shieldAttacker.get();
        paperarc$shieldAttacker.remove();
        if (attacker == null) {
            return; // no damager context: vanilla behaviour, no event (as Paper)
        }
        Player self = (Player) (Object) this;
        PlayerShieldDisableEvent event = new PlayerShieldDisableEvent(
            PaperArcBridge.bukkitPlayer(self),
            PaperArcBridge.bukkitEntity(attacker),
            100
        );
        PaperArcBridge.fire(event);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        this.getCooldowns().addCooldown(Items.SHIELD, event.getCooldown());
        ((LivingEntity) (Object) this).stopUsingItem();
        ((LivingEntity) (Object) this).level().broadcastEntityEvent(self, (byte) 30);
        ci.cancel(); // body re-implemented above
    }
}
