package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v.CraftSound;
import org.bukkit.craftbukkit.v.entity.CraftAbstractArrow;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.google.common.base.Preconditions;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

/**
 * Adds Paper's More-Projectile-API / Fix-PickupStatus-getting-reset additions to
 * CraftAbstractArrow.
 *
 * Paper refs: patches/server/More-Projectile-API.patch,
 * Fix-PickupStatus-getting-reset.patch.
 *
 * Mapping notes vs Paper source:
 * - NMS {@code life}/{@code soundEvent} (private) and
 *   {@code getPickupItem()}/{@code setPickupItemStack(ItemStack)} (protected) are
 *   opened by {@code paperarc.accesswidener}; the two methods stay virtual calls so
 *   subclass overrides (e.g. Trident) still dispatch.
 * - The shooter is stored by CraftBukkit in {@code Entity.projectileSource} (Arclight
 *   injects that field on Entity, not Projectile — the old reflective lookup on
 *   Projectile never resolved), so this delegates to CraftBukkit's
 *   {@code Projectile#setShooter} instead of touching the field.
 * - Paper's {@code setOwner(Entity, boolean)} NMS overload does not exist in vanilla:
 *   replicated by saving/restoring the public {@code pickup} field around the vanilla
 *   single-arg {@code setOwner(Entity)} when resetPickupStatus is false.
 */
@Mixin(CraftAbstractArrow.class)
public abstract class CraftAbstractArrowApiMixin {

    @Shadow
    public abstract AbstractArrow getHandle();

    @Unique
    public ItemStack getItemStack() {
        return CraftItemStack.asCraftMirror(this.getHandle().getPickupItem());
    }

    @Unique
    public void setItemStack(final ItemStack stack) {
        Preconditions.checkArgument(stack != null, "ItemStack cannot be null");
        this.getHandle().setPickupItemStack(CraftItemStack.asNMSCopy(stack));
    }

    @Unique
    public int getLifetimeTicks() {
        return this.getHandle().life;
    }

    @Unique
    public void setLifetimeTicks(int ticks) {
        this.getHandle().life = ticks;
    }

    @Unique
    public Sound getHitSound() {
        return CraftSound.minecraftToBukkit(this.getHandle().soundEvent);
    }

    @Unique
    public void setHitSound(Sound sound) {
        this.getHandle().setSoundEvent(CraftSound.bukkitToMinecraft(sound));
    }

    @Unique
    public void setShooter(ProjectileSource shooter, boolean resetPickupStatus) {
        AbstractArrow handle = this.getHandle();
        org.bukkit.entity.Projectile self = (org.bukkit.entity.Projectile) (Object) this;
        if (resetPickupStatus) {
            // Vanilla behaviour: setOwner also applies its pickup-status adjustment.
            self.setShooter(shooter);
        } else {
            // Replicates Paper's setOwner(Entity, false): update owner but keep pickup status.
            AbstractArrow.Pickup previous = handle.pickup;
            self.setShooter(shooter);
            handle.pickup = previous;
        }
    }
}
