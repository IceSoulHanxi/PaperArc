package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Skull} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Skull", remap = false)
public interface SkullIfaceMixin {

    @Unique
    public abstract void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile p0);

    @Unique
    public abstract com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile();
}
