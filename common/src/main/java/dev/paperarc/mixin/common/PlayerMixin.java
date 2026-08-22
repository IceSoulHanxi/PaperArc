package dev.paperarc.mixin.common;

import dev.paperarc.event.PaperArcEvents;
import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PlayerDeepSleepEvent 触发点。
 * <p>
 * 对照 Paper：Player#tick 的 isSleeping 分支内，sleepCounter 达到
 * SLEEP_DURATION(=100) 时发出；取消则把 sleepCounter 置为
 * Integer.MIN_VALUE（之后不再触发）。注意 vanilla 会把 sleepCounter
 * 封顶在 100，因此未取消时事件每个 tick 都会重复发出——与 Paper 一致。
 * <p>
 * v1 简化：注入点在 tick HEAD，比 Paper 的 sleepCounter++ 之后早半个
 * tick 触发首次事件；待真实代码核对后可改为精确位置。
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Shadow
    private int sleepCounter;

    @Inject(method = "tick", at = @At("HEAD"))
    private void paperarc$onDeepSleep(CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!serverPlayer.isSleeping() || this.sleepCounter != 100) {
            return;
        }
        org.bukkit.entity.Player bukkit = Bukkit.getPlayer(serverPlayer.getUUID());
        if (bukkit == null) {
            return;
        }
        if (PaperArcEvents.fire(new PlayerDeepSleepEvent(bukkit)).isCancelled()) {
            this.sleepCounter = Integer.MIN_VALUE;
        }
    }
}
