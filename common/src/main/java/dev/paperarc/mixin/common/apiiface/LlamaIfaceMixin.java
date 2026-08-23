package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Llama} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Llama", remap = false)
public interface LlamaIfaceMixin {

    public abstract boolean inCaravan();

    public abstract void joinCaravan(org.bukkit.entity.Llama p0);

    public abstract void leaveCaravan();

    public abstract org.bukkit.entity.Llama getCaravanHead();

    public abstract boolean hasCaravanTail();

    public abstract org.bukkit.entity.Llama getCaravanTail();
}
