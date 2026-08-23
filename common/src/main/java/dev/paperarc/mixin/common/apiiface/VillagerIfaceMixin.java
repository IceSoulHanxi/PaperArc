package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Villager} (generated).
 * Adds 8 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Villager", remap = false)
public interface VillagerIfaceMixin {

    @Unique
    public abstract boolean increaseLevel(int p0);

    @Unique
    public abstract boolean addTrades(int p0);

    @Unique
    public abstract int getRestocksToday();

    @Unique
    public abstract void setRestocksToday(int p0);

    @Unique
    public abstract com.destroystokyo.paper.entity.villager.Reputation getReputation(java.util.UUID p0);

    @Unique
    public abstract java.util.Map getReputations();

    @Unique
    public abstract void setReputation(java.util.UUID p0, com.destroystokyo.paper.entity.villager.Reputation p1);

    @Unique
    public abstract void clearReputations();
}
