package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code PlayerPickupItemEvent#getFlyAtPlayer()/setFlyAtPlayer(boolean)}。
 *
 * <p>默认 {@code true}；paper 还在 {@code setCancelled} 里顺带写
 * {@code flyAtPlayer = !cancel}（插件一取消，物品也就不飞了），那句用 {@code @Inject}
 * 挂在运行时已有的 {@code setCancelled} 的 RETURN 上补回来。
 *
 * <p>消费方见 {@code entity.ItemEntityFlyAtPlayerMixin}。
 */
@Mixin(PlayerPickupItemEvent.class)
public abstract class PlayerPickupItemEventApiMixin {

    @Unique
    private boolean paperarc$flyAtPlayer = true;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/entity/Item;I)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$register(Player player, Item item, int remaining, CallbackInfo ci) {
        this.paperarc$flyAtPlayer = true;
        PaperarcEventCauses.rememberPickupEvent((PlayerPickupItemEvent) (Object) this);
    }

    @Inject(method = "setCancelled(Z)V", at = @At("RETURN"), remap = false)
    private void paperarc$syncFlyAtPlayer(boolean cancel, CallbackInfo ci) {
        this.paperarc$flyAtPlayer = !cancel;
    }

    @Unique
    public boolean getFlyAtPlayer() {
        return this.paperarc$flyAtPlayer;
    }

    @Unique
    public void setFlyAtPlayer(boolean flyAtPlayer) {
        this.paperarc$flyAtPlayer = flyAtPlayer;
    }
}
