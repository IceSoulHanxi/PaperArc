package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v.CraftSound;
import org.bukkit.craftbukkit.v.entity.CraftArrow;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's AbstractArrow API to the 1.20.1 CraftBukkit host class
 * {@link CraftArrow} (1.21.1 renamed it to {@code CraftAbstractArrow}; the
 * bukkit-side interface is {@code org.bukkit.entity.AbstractArrow} in both).
 *
 * <p>Paper refs: patches/server/More-Projectile-API.patch (lifetimeTicks / hitSound)
 * and patches/server/Improve-Arrow-API.patch (getItemStack / noPhysics).
 *
 * <p>The NMS members Paper reaches through its own AT are widened the same way here
 * (META-INF/accesstransformer.cfg: f_36697_ = life, f_36700_ = soundEvent,
 * m_7941_ = getPickupItem) and read/written directly — string reflection against
 * mojmap names cannot work on the srg runtime.
 */
@Mixin(CraftArrow.class)
public abstract class CraftAbstractArrowApiMixin {

    @Shadow
    public abstract AbstractArrow getHandle();

    @Unique
    public ItemStack getItemStack() {
        return CraftItemStack.asCraftMirror(this.getHandle().getPickupItem());
    }

    @Unique
    public void setLifetimeTicks(int ticks) {
        this.getHandle().life = ticks;
    }

    @Unique
    public int getLifetimeTicks() {
        return this.getHandle().life;
    }

    @Unique
    public Sound getHitSound() {
        return CraftSound.getBukkit(this.getHandle().soundEvent);
    }

    @Unique
    public void setHitSound(Sound sound) {
        Preconditions.checkArgument(sound != null, "sound cannot be null");
        this.getHandle().setSoundEvent(CraftSound.getSoundEffect(sound));
    }

    /**
     * Overrides the CraftEntity-level implementation: {@code AbstractArrow#setNoPhysics}
     * additionally syncs the shared entity flag, which the plain field write does not.
     */
    @Unique
    public void setNoPhysics(boolean noPhysics) {
        this.getHandle().setNoPhysics(noPhysics);
    }

    @Unique
    public boolean hasNoPhysics() {
        return this.getHandle().isNoPhysics();
    }
}
