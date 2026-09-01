package com.ixnah.mc.paperarc.bridge;

import io.papermc.paper.potion.PotionMix;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * PotionBrewer delegating the vanilla lookup methods to the CraftBukkit base
 * {@link org.bukkit.craftbukkit.v.potion.CraftPotionBrewer} (present in
 * the deobf classpath) and keeping Paper's custom potion-mix extensions in an
 * in-memory side list (vanilla brewing registry untouched).
 *
 * <p>Lives in {@code bridge} (not the mixin package) so that the merged
 * {@code CraftServer} bytecode may reference it directly — Mixin forbids
 * referencing classes inside a defined mixin package.</p>
 */
public final class PaperarcPotionBrewer implements PotionBrewer {

    private static final org.bukkit.craftbukkit.v.potion.CraftPotionBrewer CB_BREWER =
            new org.bukkit.craftbukkit.v.potion.CraftPotionBrewer();
    private static final List<PotionMix> MIXES = new ArrayList<>();

    @Override
    public void addPotionMix(PotionMix mix) {
        synchronized (MIXES) {
            MIXES.removeIf(existing -> existing.getKey().equals(mix.getKey()));
            MIXES.add(mix);
        }
    }

    @Override
    public void removePotionMix(NamespacedKey key) {
        synchronized (MIXES) {
            MIXES.removeIf(existing -> existing.getKey().equals(key));
        }
    }

    @Override
    public void resetPotionMixes() {
        synchronized (MIXES) {
            MIXES.clear();
        }
    }

    @Override
    public Collection<PotionEffect> getEffects(PotionType type, boolean upgraded, boolean extended) {
        return CB_BREWER.getEffects(type, upgraded, extended);
    }

    @Override
    public Collection<PotionEffect> getEffectsFromDamage(int damage) {
        return CB_BREWER.getEffectsFromDamage(damage);
    }

    @Override
    public PotionEffect createEffect(PotionEffectType potion, int duration, int amplifier) {
        return CB_BREWER.createEffect(potion, duration, amplifier);
    }
}
