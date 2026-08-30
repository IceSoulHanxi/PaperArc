package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.event.entity.CreeperIgniteEvent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftCreeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Creeper ignite API.
 *
 * Paper adds {@code setIgnited(boolean)} to NMS Creeper, which writes the private
 * static {@code DATA_IS_IGNITED} EntityDataAccessor and fires CreeperIgniteEvent.
 * The accessor is widened via AT (f_32275_) and read directly — no reflection.
 */
@Mixin(CraftCreeper.class)
public abstract class CraftCreeperApiMixin {

    @Shadow
    public abstract Creeper getHandle();

    @Unique
    public boolean isIgnited() {
        return getHandle().isIgnited();
    }

    @Unique
    public void setIgnited(boolean ignited) {
        Creeper handle = getHandle();
        if (handle.isIgnited() != ignited) {
            CreeperIgniteEvent event = new CreeperIgniteEvent(
                (org.bukkit.entity.Creeper) (Object) this, ignited);
            if (event.callEvent()) {
                handle.getEntityData().set(Creeper.DATA_IS_IGNITED, event.isIgnited());
            }
        }
    }
}
