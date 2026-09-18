package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.util.UUID;

/**
 * paper 的 adventure 版 {@code kickMessage()} 与两个 {@code disallow(..., Component)}
 * （checklist §1.10 am，第 ④ 批）。
 *
 * <p>B8 批 3d 补上 {@code getHostname()}/{@code getRawAddress()}/{@code get-setPlayerProfile()}：
 * 三样都由 {@code server.ServerLoginPreLoginContextMixin} 在 Arclight 派发事件之前压 ThreadLocal
 * （hostname 来自握手包、rawAddress 来自 netty channel、profile 包装登录用的 {@code GameProfile}），
 * 构造器里取；{@code setPlayerProfile} 由同一个 mixin 在事件之后写回 {@code authenticatedProfile}。</p>
 */
@Mixin(AsyncPlayerPreLoginEvent.class)
public abstract class AsyncPlayerPreLoginEventApiMixin {

    @Unique
    private AsyncPlayerPreLoginEvent paperarc$self() {
        return (AsyncPlayerPreLoginEvent) (Object) this;
    }

    @Unique
    public Component kickMessage() {
        String legacy = this.paperarc$self().getKickMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void kickMessage(Component message) {
        this.paperarc$self().setKickMessage(
                message == null ? null : LegacyComponentSerializer.legacySection().serialize(message));
    }

    @Unique
    public void disallow(AsyncPlayerPreLoginEvent.Result result, Component message) {
        this.paperarc$self().disallow(result,
                message == null ? null : LegacyComponentSerializer.legacySection().serialize(message));
    }

    @Unique
    public void disallow(PlayerPreLoginEvent.Result result, Component message) {
        this.paperarc$self().disallow(result,
                message == null ? null : LegacyComponentSerializer.legacySection().serialize(message));
    }

    @Unique
    private String paperarc$hostname;

    @Unique
    private InetAddress paperarc$rawAddress;

    @Unique
    private PlayerProfile paperarc$profile;

    @Inject(method = "<init>(Ljava/lang/String;Ljava/net/InetAddress;Ljava/util/UUID;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$capturePreLoginContext(String name, InetAddress ipAddress, UUID uniqueId,
                                                 CallbackInfo ci) {
        this.paperarc$hostname = PaperarcEventCauses.takePreLoginHostname();
        InetAddress raw = PaperarcEventCauses.takePreLoginRawAddress();
        this.paperarc$rawAddress = raw == null ? ipAddress : raw;
        this.paperarc$profile = PaperarcEventCauses.takePreLoginProfile();
    }

    @Unique
    public String getHostname() {
        return this.paperarc$hostname;
    }

    @Unique
    public InetAddress getRawAddress() {
        return this.paperarc$rawAddress;
    }

    @Unique
    public PlayerProfile getPlayerProfile() {
        return this.paperarc$profile;
    }

    @Unique
    public void setPlayerProfile(PlayerProfile profile) {
        this.paperarc$profile = profile;
    }
}
