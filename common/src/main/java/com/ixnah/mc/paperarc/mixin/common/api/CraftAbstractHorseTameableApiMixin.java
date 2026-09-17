package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.bukkit.craftbukkit.v.entity.CraftAbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code Tameable#getOwnerUniqueId()} 的马科实现体。
 * {@code CraftTameableAnimal} 与 {@code CraftAbstractHorse} 是两条互不继承的分支，
 * 都实现 {@code Tameable}，只给前者加实现体时马/驴/骆驼等会 {@code AbstractMethodError}。
 */
@Mixin(CraftAbstractHorse.class)
public abstract class CraftAbstractHorseTameableApiMixin {

    @Shadow
    public abstract AbstractHorse getHandle();

    @Unique
    public java.util.UUID getOwnerUniqueId() {
        return this.getHandle().getOwnerUUID();
    }
}
