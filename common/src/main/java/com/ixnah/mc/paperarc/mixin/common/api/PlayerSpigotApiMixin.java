package com.ixnah.mc.paperarc.mixin.common.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code Player.Spigot#getPing()}。
 *
 * <p>paper-api 里这个基类方法本身就是 {@code throw new UnsupportedOperationException("Not supported yet.")}，
 * 真正的实现在 {@code CraftPlayer#spigot()} 返回的匿名子类上（见 {@link CraftPlayerSpigotPingMixin}）。
 * 两边都要补：只补子类的话，插件按 paper-api 编出来的
 * {@code invokevirtual org/bukkit/entity/Player$Spigot.getPing} 在解析阶段就 NoSuchMethodError。
 */
@Mixin(targets = "org.bukkit.entity.Player$Spigot", remap = false)
public abstract class PlayerSpigotApiMixin {

    @Unique
    public int getPing() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
