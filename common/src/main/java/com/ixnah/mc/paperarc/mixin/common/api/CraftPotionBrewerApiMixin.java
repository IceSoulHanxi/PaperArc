package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.LinkedHashMap;
import java.util.Map;

import io.papermc.paper.potion.PotionMix;

import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionBrewer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Custom-Potion-Mixes API to {@link CraftPotionBrewer}:
 * {@code addPotionMix}/{@code removePotionMix}/{@code resetPotionMixes}.
 *
 * <p>Vanilla 1.20.1 {@code PotionBrewing} has no {@code addPotionMix/removePotionMix/
 * resetPotionMixes} (Paper patch members). Since brewing result computation cannot be
 * hooked without those NMS members, the mixes are kept in a side map for API
 * compatibility — recipe lookup in the vanilla brewing pipeline is not affected.</p>
 */
@Mixin(CraftPotionBrewer.class)
public abstract class CraftPotionBrewerApiMixin {

    @Unique
    private static final Map<NamespacedKey, PotionMix> PAPERARC$MIXES = new LinkedHashMap<>();

    @Unique
    public void addPotionMix(PotionMix mix) {
        PAPERARC$MIXES.put(mix.getKey(), mix);
    }

    @Unique
    public void removePotionMix(NamespacedKey key) {
        PAPERARC$MIXES.remove(key);
    }

    @Unique
    public void resetPotionMixes() {
        PAPERARC$MIXES.clear();
    }
}
