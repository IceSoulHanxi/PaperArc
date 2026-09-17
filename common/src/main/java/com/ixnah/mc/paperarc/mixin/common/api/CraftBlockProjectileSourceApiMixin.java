package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.projectiles.CraftBlockProjectileSource;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code ProjectileSource} 的第二个实现族（发射器/投掷器方块）。
 *
 * <p>Paper 的 {@code launchProjectile(Class, Vector, Consumer)} 实现体原先只挂在
 * {@code CraftLivingEntity} 上，方块发射源走到就是 {@code AbstractMethodError}（A4-1 r）。
 * 与那边一样：转调 spigot 基线的两参重载，再把结果交给 consumer。
 */
@Mixin(CraftBlockProjectileSource.class)
public abstract class CraftBlockProjectileSourceApiMixin {

    @Shadow
    public abstract <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity);

    @Unique
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity,
            Consumer<T> function) {
        T launch = this.launchProjectile(projectile, velocity);
        if (function != null) {
            function.accept(launch);
        }
        return launch;
    }
}
