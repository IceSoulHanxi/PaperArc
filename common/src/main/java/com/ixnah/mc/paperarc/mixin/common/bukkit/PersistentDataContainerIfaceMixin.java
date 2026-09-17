package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.persistence.PersistentDataContainer;
import io.papermc.paper.persistence.PersistentDataContainerView;
import java.io.IOException;
import org.bukkit.NamespacedKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.persistence.PersistentDataContainer}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.persistence.PersistentDataContainer", remap = false)
public interface PersistentDataContainerIfaceMixin extends io.papermc.paper.persistence.PersistentDataContainerView {

    @Unique
    public abstract void readFromBytes(byte[] p0, boolean p1);

    @Unique
    public default void readFromBytes(byte [] bytes) throws IOException {
        PersistentDataContainer self = (PersistentDataContainer) this;
        self.readFromBytes(bytes, true);
    }
}
