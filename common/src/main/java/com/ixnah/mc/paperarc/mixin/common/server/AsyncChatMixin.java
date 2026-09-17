package com.ixnah.mc.paperarc.mixin.common.server;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Paper 的 {@code AsyncChatDecorateEvent} + {@code AsyncChatEvent}（Adventure.patch /
 * Chat 相关补丁）。Arclight 两个都没有 —— 1.20+ 的聊天插件主流监听 {@code AsyncChatEvent}，
 * 在 Arclight 上全部静默失效（两分支 0 引用，docs 也从未登记）。
 *
 * <p><b>锚点。</b>Arclight 在 {@code ServerPlayNetHandlerMixin} 里往
 * {@code ServerGamePacketListenerImpl} 合并了一个自己的方法
 * {@code chat(String, PlayerChatMessage, boolean)}（arclight-common 源码 1084 行，
 * 由 {@code @Overwrite} 的 {@code broadcastChatMessage} 调用，内部 new
 * {@code AsyncPlayerChatEvent}）。它在编译期不存在，无法生成 refmap，因此
 * {@code remap = false} + 方法描述符写全 —— 1.17+ 的 srg 只重命名成员不重命名类，
 * 所以描述符里的类名与 mojmap 一致（见状态文档第五章）。
 *
 * <p><b>为什么用 {@code @WrapMethod}。</b>需要在同一个注入里做三件事：按 Paper 顺序
 * fire 两个事件、事件取消时整段不执行、事件改了消息时把新消息换进 Arclight 的原流程。
 * {@code @Inject} 改不了入参，{@code @ModifyVariable} 取消不了 —— {@code @WrapMethod}
 * 拿到 {@code Operation} 后三者都能做。
 *
 * <p><b>与 Paper 的差异（都是 Arclight 侧缺机制导致的）：</b>
 * <ul>
 *   <li>没有监听者时整段短路，Arclight 原流程一字不动 —— 零开销、零行为变化。</li>
 *   <li>{@code signedMessage()} 恒为 null：Arclight 不把 vanilla 的
 *       {@code PlayerChatMessage} 签名链暴露成 adventure 的 {@code SignedMessage}。</li>
 *   <li>渲染器/viewers 没被插件改动时，走 Arclight 原流程（{@code AsyncPlayerChatEvent}
 *       + Spigot 广播）并把 {@code message()} 以 legacy-section 形式写回，
 *       这样传统聊天插件继续有效、消息修改也能传递下去。</li>
 *   <li>一旦插件换了 {@code ChatRenderer} 或改了 {@code viewers}，legacy 事件表达不了，
 *       就由本方法自己按 viewer 渲染并发送，{@code AsyncPlayerChatEvent} 不再触发
 *       —— 与 Paper 的"现代链路取代传统链路"一致。</li>
 *   <li>{@code async} 参数为 false（理论上的主线程调用）时事件同样以 sync 形式 fire，
 *       否则 Bukkit 会拒绝从主线程 fire 异步事件。</li>
 * </ul>
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class AsyncChatMixin {

    @Shadow
    public ServerPlayer player;

    @WrapMethod(method = "chat(Ljava/lang/String;Lnet/minecraft/network/chat/PlayerChatMessage;Z)V",
            remap = false)
    private void paperarc$asyncChat(String s, PlayerChatMessage original, boolean async,
            Operation<Void> operation) {
        if (s == null || s.isEmpty() || (!async && s.startsWith("/"))
                || (AsyncChatEvent.getHandlerList().getRegisteredListeners().length == 0
                    && AsyncChatDecorateEvent.getHandlerList().getRegisteredListeners().length == 0)) {
            operation.call(s, original, async);
            return;
        }
        Player sender = Bukkit.getPlayer(this.player.getUUID());
        if (sender == null) {
            operation.call(s, original, async);
            return;
        }

        Component originalMessage = Component.text(s);
        Component message = originalMessage;

        AsyncChatDecorateEvent decorate =
                new AsyncChatDecorateEvent(async, sender, originalMessage, originalMessage);
        Bukkit.getPluginManager().callEvent(decorate);
        if (!decorate.isCancelled() && decorate.result() != null) {
            message = decorate.result();
        }

        Set<Audience> viewers = new LinkedHashSet<>(Bukkit.getOnlinePlayers());
        viewers.add(Bukkit.getConsoleSender());
        Set<Audience> originalViewers = Set.copyOf(viewers);
        ChatRenderer renderer = ChatRenderer.defaultRenderer();

        AsyncChatEvent event = new AsyncChatEvent(async, sender, viewers, renderer,
                message, originalMessage, null);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (event.renderer() == renderer && event.viewers().equals(originalViewers)) {
            operation.call(LegacyComponentSerializer.legacySection().serialize(event.message()),
                    original, async);
            return;
        }

        Component displayName = sender.displayName();
        for (Audience viewer : event.viewers()) {
            viewer.sendMessage(event.renderer().render(sender, displayName, event.message(), viewer));
        }
    }
}
