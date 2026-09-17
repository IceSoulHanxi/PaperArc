package com.ixnah.mc.paperarc.mixin.common.server;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper 的 {@code AsyncTabCompleteEvent}（patches/server/AsyncTabCompleteEvent.patch）。
 * Arclight 完全没有实现，异步补全类插件全部失效。
 *
 * <p>锚点是 vanilla 的 {@code handleCustomCommandSuggestions}（Arclight 没有
 * overwrite 它，源码核对过），所以走正常 refmap，不需要 srg 字面量。
 *
 * <p><b>为什么要判主线程再 fire。</b>该方法的第一句是
 * {@code PacketUtils.ensureRunningOnSameThread(...)}：它先把自己重新投递到主线程、
 * 再抛 {@code RunningOnDifferentThreadException} 结束当前这趟。也就是说方法体会被执行
 * <b>两次</b> —— 第一次在 netty 线程，第二次在主线程。{@code @At("HEAD")} 在
 * ensureRunningOnSameThread 之前，因此：
 * <ul>
 *   <li>netty 线程那一趟：事件按 Paper 语义异步 fire；被 handle 了就直接回包并
 *       {@code ci.cancel()}，方法提前返回、根本不会重新投递，vanilla 补全不再执行。</li>
 *   <li>主线程那一趟（事件没被 handle 时才会走到）：必须跳过，否则从主线程 fire
 *       异步事件会被 Bukkit 直接拒绝（IllegalStateException）。</li>
 * </ul>
 *
 * <p>与 Paper 的差异：Paper 是把 {@code ensureRunningOnSameThread} 整句删掉后自行
 * {@code scheduleOnMain}，这里保留 vanilla 的重新投递机制，因此事件未被 handle 时
 * 会比 Paper 多走一次 netty→主线程的调度（行为一致，只是多一次投递）。
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class AsyncTabCompleteMixin {

    @Shadow
    public ServerPlayer player;

    @Shadow
    public abstract void send(Packet<?> packet);

    @Inject(method = "handleCustomCommandSuggestions", at = @At("HEAD"), cancellable = true)
    private void paperarc$asyncTabComplete(ServerboundCommandSuggestionPacket packet, CallbackInfo ci) {
        if (this.player.getServer() == null || this.player.getServer().isSameThread()) {
            return; // 主线程那一趟：事件已经在 netty 线程 fire 过了
        }
        Player bukkitPlayer = Bukkit.getPlayer(this.player.getUUID());
        if (bukkitPlayer == null) {
            return;
        }
        String buffer = packet.getCommand();
        AsyncTabCompleteEvent event = new AsyncTabCompleteEvent(bukkitPlayer, buffer, true, null);
        try {
            Bukkit.getPluginManager().callEvent(event);
        } catch (Throwable t) {
            // 事件分发出问题不能吃掉玩家的补全请求，退回 vanilla 流程
            return;
        }
        List<AsyncTabCompleteEvent.Completion> completions =
                event.isCancelled() ? List.of() : event.completions();
        if (!event.isHandled()) {
            if (event.isCancelled()) {
                ci.cancel(); // 取消但没提供结果：不回包，也不走 vanilla
            }
            return;
        }
        if (!completions.isEmpty()) {
            // 替换范围从最后一个空格之后开始，与 Paper 一致
            SuggestionsBuilder builder = new SuggestionsBuilder(buffer, buffer.lastIndexOf(' ') + 1);
            for (AsyncTabCompleteEvent.Completion completion : completions) {
                Component tooltip = completion.tooltip();
                if (tooltip == null) {
                    builder.suggest(completion.suggestion());
                } else {
                    builder.suggest(completion.suggestion(), net.minecraft.network.chat.Component.Serializer
                            .fromJson(GsonComponentSerializer.gson().serialize(tooltip)));
                }
            }
            send(new ClientboundCommandSuggestionsPacket(packet.getId(), builder.build()));
        }
        ci.cancel();
    }
}
