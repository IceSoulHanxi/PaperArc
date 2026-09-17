package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.inventory.CraftMetaCompass;
import org.bukkit.inventory.meta.CompassMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Paper 的 {@code CompassMeta#isLodestoneCompass()/clearLodestone()}。 */
@Mixin(CraftMetaCompass.class)
public abstract class CraftMetaCompassApiMixin {

    @Unique
    public boolean isLodestoneCompass() {
        return ((CompassMeta) (Object) this).hasLodestone();
    }

    @Unique
    public void clearLodestone() {
        ((CompassMeta) (Object) this).setLodestone(null);
    }
}
