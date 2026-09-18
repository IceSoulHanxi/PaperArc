package com.ixnah.mc.paperarc.mixin.common.server;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.ixnah.mc.paperarc.bridge.ConnectionBridge;
import com.ixnah.mc.paperarc.bridge.CraftPlayerProfile;
import com.ixnah.mc.paperarc.bridge.EventCauseState;
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
 * <p>Arclight 在自己合并进来的 {@code arclight$preLogin()} 里构造事件，形参只有
 * name/address/uuid（spigot 的老签名），所以：
 * <ul>
 *   <li>HEAD：把 hostname（握手包里的虚拟主机，{@code ConnectionFieldsMixin} 已记下）、
 *       rawAddress（netty channel 的对端地址，绕过 BungeeCord 改写）、
 *       以及登录用的 {@code GameProfile} 包装成 {@code PlayerProfile} 压进 ThreadLocal；</li>
 *   <li>事件触发之后：若插件换了 {@code setPlayerProfile(...)}，把它写回
 *       {@code ServerLoginPacketListenerImpl.gameProfile} —— 后续建玩家用的就是这个字段。</li>
 * </ul>
 *
 * <p>{@code arclight$preLogin} 里只有一处 {@code PluginManager#callEvent}
 * （同步那份在内部类 {@code SyncPreLogin#evaluate} 里，是另一个方法）。
 */
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPreLoginContextMixin {

    @Shadow
    @Final
    public Connection connection;

    @Shadow
    private GameProfile gameProfile;

    @Inject(method = "arclight$preLogin", at = @At("HEAD"), remap = false)
    private void paperarc$pushPreLoginContext(CallbackInfo ci) {
        InetSocketAddress virtualHost = ((ConnectionBridge) this.connection).paper$getVirtualHost();
        String hostname = virtualHost == null ? null
                : virtualHost.getHostString() + ":" + virtualHost.getPort();
        EventCauseState.setPreLoginContext(hostname,
                ((ConnectionBridge) this.connection).paper$getRawAddress(),
                CraftPlayerProfile.asBukkitMirror(this.gameProfile));
    }

    @Inject(method = "arclight$preLogin", remap = false,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lorg/bukkit/plugin/PluginManager;callEvent(Lorg/bukkit/event/Event;)V"))
    private void paperarc$applyPreLoginProfile(CallbackInfo ci,
                                               @Local AsyncPlayerPreLoginEvent event) {
        EventCauseState.clearPreLoginContext();
        PlayerProfile profile = event.getPlayerProfile();
        if (profile != null) {
            GameProfile replaced = CraftPlayerProfile.asAuthlibCopy(profile);
            if (!replaced.equals(this.gameProfile)) {
                this.gameProfile = replaced;
            }
        }
    }
}
