package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.advancement.Advancement} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.advancement.Advancement", remap = false)
public interface AdvancementIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.Component displayName();

    @Unique
    public abstract org.bukkit.advancement.Advancement getParent();

    @Unique
    public abstract java.util.Collection getChildren();

    @Unique
    public abstract org.bukkit.advancement.Advancement getRoot();
}
