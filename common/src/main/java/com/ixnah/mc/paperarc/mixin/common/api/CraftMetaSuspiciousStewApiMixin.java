package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import io.papermc.paper.potion.SuspiciousEffectEntry;
import org.bukkit.craftbukkit.v.inventory.CraftMetaSuspiciousStew;
import org.bukkit.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** Paper 的 {@code SuspiciousStewMeta#addCustomEffect(SuspiciousEffectEntry, boolean)}。 */
@Mixin(CraftMetaSuspiciousStew.class)
public abstract class CraftMetaSuspiciousStewApiMixin {

    @Shadow
    public abstract boolean addCustomEffect(PotionEffect effect, boolean overwrite);

    @Unique
    public boolean addCustomEffect(SuspiciousEffectEntry entry, boolean overwrite) {
        Preconditions.checkArgument(entry != null, "SuspiciousEffectEntry cannot be null");
        // SuspiciousEffectEntry 只带 type + duration，等价的 PotionEffect 取 amplifier 0
        return this.addCustomEffect(new PotionEffect(entry.effect(), entry.duration(), 0), overwrite);
    }
}
