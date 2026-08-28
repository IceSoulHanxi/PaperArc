package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.OfflinePlayer} (generated, trimmed for 1.20.1).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.OfflinePlayer", remap = false)
public interface OfflinePlayerIfaceMixin {

    @Unique
    public abstract boolean isConnected();

    @Unique
    public abstract long getLastLogin();

    @Unique
    public abstract long getLastSeen();
}
