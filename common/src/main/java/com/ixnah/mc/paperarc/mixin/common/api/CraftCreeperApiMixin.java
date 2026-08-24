package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.event.entity.CreeperIgniteEvent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;
import org.bukkit.craftbukkit.v.entity.CraftCreeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Creeper ignite API.
 *
 * Paper adds {@code setIgnited(boolean)} to NMS Creeper, which writes the private
 * static {@code DATA_IS_IGNITED} EntityDataAccessor and fires CreeperIgniteEvent.
 * The accessor is fetched reflectively (mojmap runtime name: DATA_IS_IGNITED).
 */
@Mixin(CraftCreeper.class)
public abstract class CraftCreeperApiMixin {

    @Shadow
    public abstract Creeper getHandle();

    @Unique
    private static volatile EntityDataAccessor<Boolean> PAPERARC$DATA_IS_IGNITED;

    @Unique
    @SuppressWarnings("unchecked")
    private static EntityDataAccessor<Boolean> paperarc$dataIsIgnited() {
        EntityDataAccessor<Boolean> acc = PAPERARC$DATA_IS_IGNITED;
        if (acc == null) {
            synchronized (CraftCreeperApiMixin.class) {
                if (PAPERARC$DATA_IS_IGNITED == null) {
                    try {
                        java.lang.reflect.Field f = Creeper.class.getDeclaredField("DATA_IS_IGNITED");
                        f.setAccessible(true);
                        PAPERARC$DATA_IS_IGNITED = (EntityDataAccessor<Boolean>) f.get(null);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Creeper.DATA_IS_IGNITED field not found", e);
                    }
                }
                acc = PAPERARC$DATA_IS_IGNITED;
            }
        }
        return acc;
    }

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
                handle.getEntityData().set(paperarc$dataIsIgnited(), event.isIgnited());
            }
        }
    }
}
