package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Sign} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Sign", remap = false)
public interface SignIfaceMixin {

    public abstract java.util.List lines();

    public abstract java.util.UUID getAllowedEditorUniqueId();

    public abstract void setAllowedEditorUniqueId(java.util.UUID p0);

    public abstract org.bukkit.block.sign.Side getInteractableSideFor(double p0, double p1);
}
