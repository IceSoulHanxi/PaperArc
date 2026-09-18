package com.ixnah.mc.paperarc.mixin.common.server;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.ixnah.mc.paperarc.bridge.ConnectionBridge;
import com.ixnah.mc.paperarc.bridge.CraftPlayerProfile;
import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

/**
 * Paper 的 {@code AsyncPlayerPreLoginEvent} 三样附加上下文在登录侧的来源与消费方。
 *
 * <p>Arclight 在自己合并进来的 {@code callPlayerPreLoginEvents(GameProfile)} 里构造事件，
 * 形参只有 name/address/uuid（spigot 老签名），所以：
 * <ul>
 *   <li>HEAD：把 hostname（握手包里的虚拟主机，{@code network.ConnectionFieldsMixin} 已记下）、
 *       rawAddress（netty channel 的对端地址，绕过 BungeeCord 改写）、
 *       以及登录用的 {@code GameProfile} 包装成 {@code PlayerProfile} 压 ThreadLocal；</li>
 *   <li>事件派发之后：若插件换了 {@code setPlayerProfile(...)}，写回
 *       {@code authenticatedProfile} —— 后续建玩家用的就是这个字段。</li>
 * </ul>
 *
 * <p>{@code callPlayerPreLoginEvents} 是 Arclight 自加的普通 {@code @Unique} 方法
 * （不是注入器 handler，名字稳定），按约定只写方法名 + {@code remap = false}。
 * 方法里只有第一处 {@code PluginManager#callEvent} 是异步那份，同步那份在
 * 内部类 {@code SyncPreLogin} 里，是另一个方法。
 */
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPreLoginContextMixin {

    @Shadow
    @Final
    Connection connection;

    @Shadow
    private GameProfile authenticatedProfile;

    @Inject(method = "callPlayerPreLoginEvents", at = @At("HEAD"), remap = false)
    private void paperarc$pushPreLoginContext(GameProfile profile, CallbackInfo ci) {
        InetSocketAddress virtualHost = ((ConnectionBridge) this.connection).paper$getVirtualHost();
        String hostname = virtualHost == null ? null
                : virtualHost.getHostString() + ":" + virtualHost.getPort();
        PaperarcEventCauses.pushPreLoginContext(hostname,
                ((ConnectionBridge) this.connection).paper$getRawAddress(),
                CraftPlayerProfile.asBukkitMirror(profile));
    }

    @Inject(method = "callPlayerPreLoginEvents", remap = false,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lorg/bukkit/plugin/PluginManager;callEvent(Lorg/bukkit/event/Event;)V"))
    private void paperarc$applyPreLoginProfile(GameProfile profile, CallbackInfo ci,
                                               @Local AsyncPlayerPreLoginEvent event) {
        PaperarcEventCauses.popPreLoginContext();
        PlayerProfile replacement = event.getPlayerProfile();
        if (replacement != null) {
            GameProfile replaced = CraftPlayerProfile.asAuthlibCopy(replacement);
            if (!replaced.equals(this.authenticatedProfile)) {
                this.authenticatedProfile = replaced;
            }
        }
    }
}
