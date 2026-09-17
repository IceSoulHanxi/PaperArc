package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.projectiles.CraftBlockProjectileSource;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 {@code ProjectileSource#launchProjectile(Class, Vector, Consumer)}。
 *
 * <p>偏差：Paper 在实体加入世界**之前**执行 consumer，Arclight 没有对应的
 * 预生成回调，这里在发射之后立即执行 —— 对绝大多数用法（改元数据）等价，
 * 但依赖"生成事件里就能看到改动"的插件会有差异。</p>
 */
@Mixin(CraftBlockProjectileSource.class)
public abstract class CraftBlockProjectileSourceApiMixin {

    @Unique
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity,
                                                     java.util.function.Consumer<? super T> function) {
        T launched = ((ProjectileSource) (Object) this).launchProjectile(projectile, velocity);
        if (function != null && launched != null) {
            function.accept(launched);
        }
        return launched;
    }
}
