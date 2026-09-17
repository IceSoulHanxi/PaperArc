package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.ProjectileBridge;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 暴露 {@code Projectile.hasBeenShot}。
 *
 * <p>这个字段 vanilla/Arclight 自己就有（private），原先写成 {@code @Unique public} 是
 * 重复声明，Mixin 只打一行 {@code Discarding @Unique public field} 的 WARN 就丢掉
 * ——访问器碰巧还是读到了 NMS 那份，属于蒙对（B3 把这条 WARN 升成门禁后暴露）。
 * 改成 {@code @Shadow}，明确读写的就是 NMS 的字段。
 */
@Mixin(Projectile.class)
public abstract class ProjectileFieldsMixin implements ProjectileBridge {

    @Shadow
    private boolean hasBeenShot;

    @Override
    public boolean paper$hasBeenShot() {
        return this.hasBeenShot;
    }

    @Override
    public void paper$setHasBeenShot(boolean hasBeenShot) {
        this.hasBeenShot = hasBeenShot;
    }
}
