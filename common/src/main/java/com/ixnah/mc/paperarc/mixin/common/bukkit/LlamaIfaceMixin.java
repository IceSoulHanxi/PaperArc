package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Llama} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Llama", remap = false)
public interface LlamaIfaceMixin {

    @Unique
    public abstract boolean inCaravan();

    @Unique
    public abstract void joinCaravan(org.bukkit.entity.Llama p0);

    @Unique
    public abstract void leaveCaravan();

    @Unique
    public abstract org.bukkit.entity.Llama getCaravanHead();

    @Unique
    public abstract boolean hasCaravanTail();

    @Unique
    public abstract org.bukkit.entity.Llama getCaravanTail();
}
