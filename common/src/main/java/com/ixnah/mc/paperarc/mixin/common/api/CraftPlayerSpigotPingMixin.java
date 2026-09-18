package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code Player.Spigot#getPing()} 的真实实现：{@code CraftPlayer#spigot()} 返回的匿名子类，
 * 转发到 {@code CraftPlayer#getPing()}（Spigot-API 本来就有）。
 *
 * <p><b>1.21.1 的匿名类序号与 1.20.1 不同</b>：这里是 {@code CraftPlayer$2}
 * （`javap` 核对：{@code CraftPlayer$1} 是 {@code BorderChangeListener}，
 * {@code CraftPlayer$2 extends Player$Spigot}）；1.20.1 那边是 {@code CraftPlayer$1}。
 * 搬运时必须逐版本 javap，不能照抄序号。
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.entity.CraftPlayer$2", remap = false)
public abstract class CraftPlayerSpigotPingMixin {

    @Shadow(remap = false)
    @Final
    CraftPlayer this$0;

    @Unique
    public int getPing() {
        return this.this$0.getPing();
    }
}
