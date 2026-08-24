package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.WanderingTrader} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.WanderingTrader", remap = false)
public interface WanderingTraderIfaceMixin {

    @Unique
    public abstract void setCanDrinkPotion(boolean p0);

    @Unique
    public abstract boolean canDrinkPotion();

    @Unique
    public abstract void setCanDrinkMilk(boolean p0);

    @Unique
    public abstract boolean canDrinkMilk();

    @Unique
    public abstract org.bukkit.Location getWanderingTowards();

    @Unique
    public abstract void setWanderingTowards(org.bukkit.Location p0);
}
