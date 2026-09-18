package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code org.bukkit.Bukkit} 静态门面上的方法（checklist §1.10 am / §1.11 as）。
 *
 * <p>**B5-1 走通实验阶段：只放一条方法。** paper 版 Bukkit 的方法体都是
 * {@code server.xxx(...)} 一行转发，Server 侧在 B/B2/B3 已补齐。</p>
 *
 * <p>写成 {@code @Unique private static} + {@code @Widen}：Mixin 的
 * {@code checkMethodVisibility} 拒绝合并非 private 的 static 方法，
 * 放宽由 {@code bridge.WidenPostProcessor} 在 postApply 阶段做。</p>
 */
@Mixin(Bukkit.class)
public abstract class BukkitApiMixin {

    @Unique
    @Widen(because = "paper-api: public static AsyncScheduler getAsyncScheduler()")
    private static io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() {
        return Bukkit.getServer().getAsyncScheduler();
    }
}
