package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.bukkit.craftbukkit.v.entity.CraftMushroomCow;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Port of Paper's Suspicious-Effect-Entry-API additions on
 * {@link CraftMushroomCow}: {@code addEffectToNextStew(SuspiciousEffectEntry,
 * boolean)}, {@code getStewEffects()} and {@code setStewEffects(List)}.
 *
 * The NMS backing field {@code MushroomCow#stewEffects} is private (Paper
 * accesses it directly from within the class) and opened by
 * {@code paperarc.accesswidener}.
 */
@Mixin(CraftMushroomCow.class)
public abstract class CraftMushroomCowApiMixin {

    @Shadow
    public abstract net.minecraft.world.entity.animal.MushroomCow getHandle();

    @Shadow
    public abstract boolean hasEffectForNextStew(PotionEffectType type);

    @Shadow
    public abstract boolean removeEffectFromNextStew(PotionEffectType type);

    @Unique
    public boolean addEffectToNextStew(io.papermc.paper.potion.SuspiciousEffectEntry suspiciousEffectEntry,
                                       boolean overwrite) {
        Preconditions.checkArgument(suspiciousEffectEntry != null, "SuspiciousEffectEntry cannot be null");
        Holder<MobEffect> minecraftPotionEffect =
            CraftPotionEffectType.bukkitToMinecraftHolder(suspiciousEffectEntry.effect());
        if (!overwrite && this.hasEffectForNextStew(suspiciousEffectEntry.effect())) {
            return false;
        }
        net.minecraft.world.entity.animal.MushroomCow handle = this.getHandle();
        SuspiciousStewEffects stewEffects = handle.stewEffects;
        if (stewEffects == null) {
            stewEffects = SuspiciousStewEffects.EMPTY;
        }
        SuspiciousStewEffects.Entry recordSuspiciousEffect =
            new SuspiciousStewEffects.Entry(minecraftPotionEffect, suspiciousEffectEntry.duration());
        this.removeEffectFromNextStew(suspiciousEffectEntry.effect()); // Avoid duplicates of effects
        handle.stewEffects = stewEffects.withEffectAdded(recordSuspiciousEffect);
        return true;
    }

    @Unique
    public List<io.papermc.paper.potion.SuspiciousEffectEntry> getStewEffects() {
        SuspiciousStewEffects stewEffects = this.getHandle().stewEffects;
        if (stewEffects == null) {
            return List.of();
        }
        List<io.papermc.paper.potion.SuspiciousEffectEntry> effectEntries =
            new ArrayList<>(stewEffects.effects().size());
        for (SuspiciousStewEffects.Entry effect : stewEffects.effects()) {
            effectEntries.add(io.papermc.paper.potion.SuspiciousEffectEntry.create(
                CraftPotionEffectType.minecraftHolderToBukkit(effect.effect()),
                effect.duration()));
        }
        return Collections.unmodifiableList(effectEntries);
    }

    @Unique
    public void setStewEffects(List<io.papermc.paper.potion.SuspiciousEffectEntry> effects) {
        Preconditions.checkArgument(effects != null, "effects cannot be null");
        net.minecraft.world.entity.animal.MushroomCow handle = this.getHandle();
        if (effects.isEmpty()) {
            handle.stewEffects = null;
            return;
        }
        List<SuspiciousStewEffects.Entry> nmsPairs = new ArrayList<>(effects.size());
        for (io.papermc.paper.potion.SuspiciousEffectEntry effect : effects) {
            nmsPairs.add(new SuspiciousStewEffects.Entry(
                CraftPotionEffectType.bukkitToMinecraftHolder(effect.effect()),
                effect.duration()));
        }
        handle.stewEffects = new SuspiciousStewEffects(nmsPairs);
    }

}
