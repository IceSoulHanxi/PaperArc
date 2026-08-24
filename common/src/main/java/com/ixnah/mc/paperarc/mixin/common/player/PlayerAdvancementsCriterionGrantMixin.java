package com.ixnah.mc.paperarc.mixin.common.player;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v.advancement.CraftAdvancement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's Add-PlayerAdvancementCriterionGrantEvent patch.
 *
 * Wraps the {@code AdvancementProgress#grantProgress} call inside
 * {@code PlayerAdvancements#award}: after a successful grant, fires
 * {@link PlayerAdvancementCriterionGrantEvent}; if cancelled, revokes the
 * criterion and reports failure — same grant-then-revoke semantics as Paper.
 */
@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsCriterionGrantMixin {

    @WrapOperation(
        method = "award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/AdvancementProgress;grantProgress(Ljava/lang/String;)Z"
        )
    )
    private boolean paperarc$onCriterionGrant(AdvancementProgress progress, String criterion, Operation<Boolean> original,
                                              AdvancementHolder holder, String criterionName) {
        boolean granted = original.call(progress, criterion);
        if (!granted) {
            return false;
        }

        ServerPlayer self = (ServerPlayer) (Object) this;
        PlayerAdvancementCriterionGrantEvent event = new PlayerAdvancementCriterionGrantEvent(
            PaperArcBridge.bukkitPlayer(self),
            new CraftAdvancement(holder),
            criterionName
        );
        PaperArcBridge.fire(event);

        if (event.isCancelled()) {
            progress.revokeProgress(criterionName);
            return false;
        }
        return true;
    }
}
