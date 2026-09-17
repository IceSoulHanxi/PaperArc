package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Sign} (generated, trimmed for 1.20.1).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Sign", remap = false)
public interface SignIfaceMixin {

    @Unique
    public abstract java.util.List lines();

    @Unique
    public abstract org.bukkit.block.sign.Side getInteractableSideFor(double p0, double p1);
    // 实现体在 CraftSignApiMixin（转调 getSide(FRONT).line(...)），接口上缺声明（A4-1 NO_DECL）。
    @Unique
    public abstract net.kyori.adventure.text.Component line(int p0);

    @Unique
    public abstract void line(int p0, net.kyori.adventure.text.Component p1);

}
