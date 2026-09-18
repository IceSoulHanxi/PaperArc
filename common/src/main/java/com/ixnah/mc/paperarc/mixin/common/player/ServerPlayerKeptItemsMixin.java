package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.api.PaperarcDeathEvents;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 兑现 {@code PlayerDeathEvent#getItemsToKeep()}（gaps.md §3.1「死亡一族」）。
 *
 * <p>Arclight 的重生流程里，{@code ServerPlayerMixin#arclight$restoreFromDeath(ServerPlayer)}
 * 在新玩家实体上把上一具身体的经验/背包（keepInventory 时）搬过来。逐件保留这条路 Arclight
 * 没有，这里在它的 TAIL 补上：把事件里点名要留的物品塞进新背包。
 *
 * <p>{@code arclight$restoreFromDeath} 是 Arclight 合并进目标的普通方法（不是注入器
 * handler，Mixin 不会给它改名），按名字选是稳的；{@code require} 用默认 1，
 * Arclight 改名字就会启动即失败而不是静默失效。
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerKeptItemsMixin {

    @Inject(method = "arclight$restoreFromDeath", at = @At("TAIL"), remap = false)
    private void paperarc$restoreKeptItems(ServerPlayer oldPlayer, CallbackInfo ci) {
        PaperarcDeathEvents.restoreKeptItems((ServerPlayer) (Object) this, oldPlayer);
    }
}
