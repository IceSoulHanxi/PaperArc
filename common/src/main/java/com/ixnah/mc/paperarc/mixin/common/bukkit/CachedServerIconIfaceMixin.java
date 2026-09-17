package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.util.CachedServerIcon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.util.CachedServerIcon}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.util.CachedServerIcon", remap = false)
public interface CachedServerIconIfaceMixin {

    @Unique
    public abstract java.lang.String getData();

    @Unique
    public default boolean isEmpty() {
        CachedServerIcon self = (CachedServerIcon) this;
        return self.getData() == null;
    }
}
