package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Cat} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>A4-3 父接口差集：补上 paper 声明的父接口 {@code io.papermc.paper.entity.CollarColorable}。父接口的两个方法运行时本来就有，只补继承关系。</p>
 */
@Mixin(targets = "org.bukkit.entity.Cat", remap = false)
public interface CatIfaceMixin extends io.papermc.paper.entity.CollarColorable {

    @Unique
    public abstract void setLyingDown(boolean p0);

    @Unique
    public abstract boolean isLyingDown();

    @Unique
    public abstract void setHeadUp(boolean p0);

    @Unique
    public abstract boolean isHeadUp();
}
