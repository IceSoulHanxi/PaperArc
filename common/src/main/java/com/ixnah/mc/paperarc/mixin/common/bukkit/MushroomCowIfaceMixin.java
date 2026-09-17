package com.ixnah.mc.paperarc.mixin.common.bukkit;

import io.papermc.paper.potion.SuspiciousEffectEntry;
import java.util.List;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.MushroomCow} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.MushroomCow", remap = false)
public interface MushroomCowIfaceMixin extends io.papermc.paper.entity.Shearable {

    @Unique
    public abstract boolean addEffectToNextStew(io.papermc.paper.potion.SuspiciousEffectEntry p0, boolean p1);

    @Unique
    public abstract java.util.List getStewEffects();

    @Unique
    public abstract void setStewEffects(java.util.List p0);

    /** paper {@code PaperShearable}（B3-2），实现体在 {@code bridge.api.PaperarcEntityTraits}。 */
    @Unique
    public default boolean readyToBeSheared() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.readyToBeSheared(this);
    }

    @Unique
    public default void shear(net.kyori.adventure.sound.Sound.Source source) {
        com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.shear(this, source);
    }

    @Unique
    public default int getStewEffectDuration() {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #getStewEffects");
    }

    @Unique
    public default void setStewEffectDuration(int duration) {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #setStewEffects");
    }

    @Unique
    public default PotionEffectType getStewEffectType() {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #getStewEffects");
    }

    @Unique
    public default void setStewEffect(PotionEffectType type) {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #setStewEffects");
    }
}
