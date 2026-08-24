package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.bukkit.entity.Enderman;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Port of Paper's EndermanAttackPlayerEvent for EnderMan.isLookingAtMe.
 * Paper splits the vanilla body into isLookingAtMe_check and fires the event
 * around its result; {@code @ModifyReturnValue} reproduces the exact same
 * semantics: event starts cancelled when the vanilla stare check fails, and
 * plugins may override the outcome in either direction.
 */
@Mixin(EnderMan.class)
public abstract class EnderManAttackPlayerMixin {

    @ModifyReturnValue(method = "isLookingAtMe", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private boolean paperarc$attackPlayerEvent(boolean shouldAttack, Player player) {
        EndermanAttackPlayerEvent event = new EndermanAttackPlayerEvent(
                (Enderman) PaperArcBridge.bukkitEntity((Entity) (Object) this),
                PaperArcBridge.bukkitPlayer(player));
        event.setCancelled(!shouldAttack);
        return event.callEvent();
    }
}
