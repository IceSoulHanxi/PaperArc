package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.data.type.Bed} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 *
 * <p>getPart/setPart/isOccupied 运行时接口上本来就有，@Unique 声明会被 Mixin
 * 丢弃（日志 Discarding @Unique），已删（A4-1 s）。
 */
@Mixin(targets = "org.bukkit.block.data.type.Bed", remap = false)
public interface BedIfaceMixin {

    @Unique
    public abstract void setOccupied(boolean p0);
}
