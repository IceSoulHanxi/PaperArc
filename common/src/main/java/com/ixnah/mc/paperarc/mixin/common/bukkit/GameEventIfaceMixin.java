package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@link org.bukkit.GameEvent} 是**抽象类**不是接口，所以这里用类目标 mixin 加
 * {@code @Unique public abstract} 声明；实现体在 {@code api.CraftGameEventApiMixin}
 * （唯一的运行时子类 CraftGameEvent）。
 */
@Mixin(targets = "org.bukkit.GameEvent", remap = false)
public abstract class GameEventIfaceMixin {

    @Unique
    public abstract int getRange();

    @Unique
    public abstract int getVibrationLevel();
}
