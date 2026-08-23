package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.ThrownPotion} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.ThrownPotion", remap = false)
public interface ThrownPotionIfaceMixin {

    public abstract org.bukkit.inventory.meta.PotionMeta getPotionMeta();

    public abstract void setPotionMeta(org.bukkit.inventory.meta.PotionMeta p0);

    public abstract void splash();
}
