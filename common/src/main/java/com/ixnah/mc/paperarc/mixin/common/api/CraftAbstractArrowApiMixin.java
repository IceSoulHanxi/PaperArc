package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v.CraftSound;
import org.bukkit.craftbukkit.v.entity.CraftAbstractArrow;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.google.common.base.Preconditions;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.AbstractArrow;

/**
 * Adds Paper's More-Projectile-API / Fix-PickupStatus-getting-reset additions to
 * CraftAbstractArrow.
 *
 * Paper refs: patches/server/More-Projectile-API.patch,
 * Fix-PickupStatus-getting-reset.patch.
 *
 * Mapping notes vs Paper source:
 * - NMS {@code life} and {@code soundEvent} are private in vanilla mojmap -> reflective
 *   access (cached).
 * - {@code getPickupItem()} / {@code setPickupItemStack(ItemStack)} are protected ->
 *   invoked reflectively so subclass overrides (e.g. Trident) still dispatch.
 * - {@code Projectile#projectileSource} exists at runtime (CB patch, see
 *   CraftProjectile bytecode) but is absent from the vanilla-mapped compile jar ->
 *   reflective access.
 * - Paper's {@code setOwner(Entity, boolean)} NMS overload does not exist in vanilla:
 *   replicated by saving/restoring the public {@code pickup} field around the vanilla
 *   single-arg {@code setOwner(Entity)} when resetPickupStatus is false.
 */
@Mixin(CraftAbstractArrow.class)
public abstract class CraftAbstractArrowApiMixin {

    @Shadow
    public abstract AbstractArrow getHandle();

    @Unique
    private static volatile Field PAPERARC$LIFE_FIELD;

    @Unique
    private static volatile Field PAPERARC$SOUND_EVENT_FIELD;

    @Unique
    private static volatile Field PAPERARC$PROJECTILE_SOURCE_FIELD;

    @Unique
    private static volatile Method PAPERARC$GET_PICKUP_ITEM_METHOD;

    @Unique
    private static volatile Method PAPERARC$SET_PICKUP_ITEM_STACK_METHOD;

    @Unique
    private static Field paperarc$field(Class<?> owner, String name) {
        try {
            Field resolved = owner.getDeclaredField(name);
            resolved.setAccessible(true);
            return resolved;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: NMS field not found: " + owner.getName() + "." + name, e);
        }
    }

    @Unique
    private static Method paperarc$method(Class<?> owner, String name, Class<?>... params) {
        try {
            Method resolved = owner.getDeclaredMethod(name, params);
            resolved.setAccessible(true);
            return resolved;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: NMS method not found: " + owner.getName() + "." + name, e);
        }
    }

    @Unique
    private static Field paperarc$lifeField() {
        Field f = PAPERARC$LIFE_FIELD;
        if (f == null) {
            synchronized (CraftAbstractArrowApiMixin.class) {
                if (PAPERARC$LIFE_FIELD == null) {
                    PAPERARC$LIFE_FIELD = paperarc$field(AbstractArrow.class, "life");
                }
                f = PAPERARC$LIFE_FIELD;
            }
        }
        return f;
    }

    @Unique
    private static Field paperarc$soundEventField() {
        Field f = PAPERARC$SOUND_EVENT_FIELD;
        if (f == null) {
            synchronized (CraftAbstractArrowApiMixin.class) {
                if (PAPERARC$SOUND_EVENT_FIELD == null) {
                    PAPERARC$SOUND_EVENT_FIELD = paperarc$field(AbstractArrow.class, "soundEvent");
                }
                f = PAPERARC$SOUND_EVENT_FIELD;
            }
        }
        return f;
    }

    @Unique
    private static Field paperarc$projectileSourceField() {
        Field f = PAPERARC$PROJECTILE_SOURCE_FIELD;
        if (f == null) {
            synchronized (CraftAbstractArrowApiMixin.class) {
                if (PAPERARC$PROJECTILE_SOURCE_FIELD == null) {
                    PAPERARC$PROJECTILE_SOURCE_FIELD =
                        paperarc$field(net.minecraft.world.entity.projectile.Projectile.class, "projectileSource");
                }
                f = PAPERARC$PROJECTILE_SOURCE_FIELD;
            }
        }
        return f;
    }

    @Unique
    public ItemStack getItemStack() {
        try {
            if (PAPERARC$GET_PICKUP_ITEM_METHOD == null) {
                synchronized (CraftAbstractArrowApiMixin.class) {
                    if (PAPERARC$GET_PICKUP_ITEM_METHOD == null) {
                        PAPERARC$GET_PICKUP_ITEM_METHOD = paperarc$method(AbstractArrow.class, "getPickupItem");
                    }
                }
            }
            return CraftItemStack.asCraftMirror((net.minecraft.world.item.ItemStack) PAPERARC$GET_PICKUP_ITEM_METHOD.invoke(this.getHandle()));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: failed to read arrow pickup item", e);
        }
    }

    @Unique
    public void setItemStack(final ItemStack stack) {
        Preconditions.checkArgument(stack != null, "ItemStack cannot be null");
        try {
            if (PAPERARC$SET_PICKUP_ITEM_STACK_METHOD == null) {
                synchronized (CraftAbstractArrowApiMixin.class) {
                    if (PAPERARC$SET_PICKUP_ITEM_STACK_METHOD == null) {
                        PAPERARC$SET_PICKUP_ITEM_STACK_METHOD =
                            paperarc$method(AbstractArrow.class, "setPickupItemStack", net.minecraft.world.item.ItemStack.class);
                    }
                }
            }
            PAPERARC$SET_PICKUP_ITEM_STACK_METHOD.invoke(this.getHandle(), CraftItemStack.asNMSCopy(stack));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: failed to set arrow pickup item", e);
        }
    }

    @Unique
    public int getLifetimeTicks() {
        try {
            return paperarc$lifeField().getInt(this.getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("PaperArc: failed to read arrow lifetime ticks", e);
        }
    }

    @Unique
    public void setLifetimeTicks(int ticks) {
        try {
            paperarc$lifeField().setInt(this.getHandle(), ticks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("PaperArc: failed to write arrow lifetime ticks", e);
        }
    }

    @Unique
    public Sound getHitSound() {
        try {
            return CraftSound.minecraftToBukkit((SoundEvent) paperarc$soundEventField().get(this.getHandle()));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("PaperArc: failed to read arrow hit sound", e);
        }
    }

    @Unique
    public void setHitSound(Sound sound) {
        this.getHandle().setSoundEvent(CraftSound.bukkitToMinecraft(sound));
    }

    @Unique
    public void setShooter(ProjectileSource shooter, boolean resetPickupStatus) {
        AbstractArrow handle = this.getHandle();
        net.minecraft.world.entity.Entity nms = null;
        if (shooter instanceof CraftEntity craftEntity) {
            nms = craftEntity.getHandle();
        }
        if (resetPickupStatus) {
            // Vanilla behaviour: setOwner also applies its pickup-status adjustment.
            handle.setOwner(nms);
        } else {
            // Replicates Paper's setOwner(Entity, false): update owner but keep pickup status.
            AbstractArrow.Pickup previous = handle.pickup;
            handle.setOwner(nms);
            handle.pickup = previous;
        }
        try {
            paperarc$projectileSourceField().set(handle, shooter);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("PaperArc: failed to write projectileSource", e);
        }
    }
}
