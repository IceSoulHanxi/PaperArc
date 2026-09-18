package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code PlayerResourcePackStatusEvent#getHash()}。
 *
 * <p>paper 自己就把它标成 {@code @Deprecated}，理由是 "Hash does not seem to ever be set"：
 * 服务端只用不带 hash 的构造器构造这个事件，hash 一路是 {@code null}
 * （运行时 `javap -p` 核对：只有 {@code (Player, UUID, Status)} 一个构造器，连字段都没有）。
 * 恒返回 {@code null} 与 paper 的实际行为完全一致，不是占位。
 */
@Mixin(PlayerResourcePackStatusEvent.class)
public abstract class PlayerResourcePackStatusEventApiMixin {

    @Unique
    public String getHash() {
        return null;
    }
}
