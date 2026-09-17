package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.FallingBlock} (generated, trimmed for 1.20.1).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.FallingBlock", remap = false)
public interface FallingBlockIfaceMixin {

    @Unique
    public abstract boolean doesAutoExpire();

    @Unique
    public abstract void shouldAutoExpire(boolean p0);
    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default org.bukkit.Location getSourceLoc() {
        return ((org.bukkit.entity.Entity) this).getOrigin();
    }

}
