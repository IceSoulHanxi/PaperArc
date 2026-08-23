package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.OfflinePlayer} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.OfflinePlayer", remap = false)
public interface OfflinePlayerIfaceMixin {

    @Unique
    public abstract boolean isConnected();

    @Unique
    public abstract long getLastLogin();

    @Unique
    public abstract long getLastSeen();

    @Unique
    public abstract io.papermc.paper.persistence.PersistentDataContainerView getPersistentDataContainer();
}
