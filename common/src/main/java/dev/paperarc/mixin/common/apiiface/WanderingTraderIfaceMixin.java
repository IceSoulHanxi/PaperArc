package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.WanderingTrader} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.WanderingTrader", remap = false)
public interface WanderingTraderIfaceMixin {

    public abstract void setCanDrinkPotion(boolean p0);

    public abstract boolean canDrinkPotion();

    public abstract void setCanDrinkMilk(boolean p0);

    public abstract boolean canDrinkMilk();

    public abstract org.bukkit.Location getWanderingTowards();

    public abstract void setWanderingTowards(org.bukkit.Location p0);
}
