package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code Player.Spigot#getPing()} 的真实实现：{@code CraftPlayer#spigot()} 返回的匿名子类
 * （运行时 {@code CraftPlayer$1}，`javap` 核对它有 {@code final CraftPlayer this$0}）。
 * 转发到 {@code CraftPlayer#getPing()}（Spigot-API 本来就有）。
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.entity.CraftPlayer$1", remap = false)
public abstract class CraftPlayerSpigotPingMixin {

    @Shadow(remap = false)
    @Final
    CraftPlayer this$0;

    @Unique
    public int getPing() {
        return this.this$0.getPing();
    }
}
