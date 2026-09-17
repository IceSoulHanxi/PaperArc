package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * paper 的 {@code Server extends ForwardingAudience}（B3-2）。
 *
 * <p>{@code ForwardingAudience} 的每个 Audience 方法都是转发给 {@code audiences()} 的
 * default 实现，所以只要这一条实现对了，{@code Bukkit.getServer().sendMessage(Component)}
 * 这类调用就真的会送达（不是空实现静默丢弃）。
 * 成员与 Paper 的 {@code CraftServer#audiences()} 一致：控制台 + 全部在线玩家。
 *
 * <p>Paper 缓存了这个 Iterable（{@code Iterables.concat} 是惰性视图），这里每次新建一个
 * ArrayList —— 免得依赖 guava 的 shade 情况，代价是每次调用一次拷贝，量级是在线人数。
 */
@Mixin(CraftServer.class)
public abstract class CraftServerAudienceApiMixin {

    @Unique
    public Iterable<? extends net.kyori.adventure.audience.Audience> audiences() {
        org.bukkit.Server server = (org.bukkit.Server) (Object) this;
        List<net.kyori.adventure.audience.Audience> out = new ArrayList<>();
        out.add((net.kyori.adventure.audience.Audience) server.getConsoleSender());
        out.addAll(server.getOnlinePlayers());
        return out;
    }
}
