package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.Raid} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.Raid", remap = false)
public interface RaidIfaceMixin {

    @Unique
    public abstract int getId();

    @Unique
    public abstract org.bukkit.boss.BossBar getBossBar();
}
