package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.Panda;
import org.bukkit.craftbukkit.v.entity.CraftPanda;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Missing-Entity-API (Panda API) additions on
 * {@link CraftPanda}.
 *
 * Sneeze/unhappy counters map to public NMS accessors. The eat counter is
 * private in vanilla NMS ({@code getEatCounter}/{@code setEatCounter}); both are
 * widened via AT (m_29170_ / m_29214_) and accessed directly — no reflection.
 */
@Mixin(CraftPanda.class)
public abstract class CraftPandaApiMixin {

    @Shadow
    public abstract Panda getHandle();

    // Paper start - Missing Entity API
    @Unique
    public int getSneezeTicks() {
        return this.getHandle().getSneezeCounter();
    }

    @Unique
    public void setSneezeTicks(int ticks) {
        this.getHandle().setSneezeCounter(ticks);
    }

    @Unique
    public int getEatingTicks() {
        return this.getHandle().getEatCounter();
    }

    @Unique
    public void setEatingTicks(int ticks) {
        this.getHandle().setEatCounter(ticks);
    }

    @Unique
    public void setUnhappyTicks(int ticks) {
        this.getHandle().setUnhappyCounter(ticks);
    }

    @Unique
    public org.bukkit.entity.Panda.Gene getCombinedGene() {
        // Paper: CraftPanda.fromNms(this.getHandle().getVariant()) — gene enums
        // share identical constant names, so map by name.
        return org.bukkit.entity.Panda.Gene.valueOf(this.getHandle().getVariant().name());
    }

    @Unique
    public boolean isSitting() {
        return this.getHandle().isSitting();
    }

    @Unique
    public void setSitting(boolean sitting) {
        this.getHandle().sit(sitting);
    }
    // Paper end - Missing Entity API
}
