package dev.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.bukkit.craftbukkit.v.entity.CraftMushroomCow;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.bukkit.potion.PotionEffectType;
import dev.paperarc.bridge.craft.CraftEntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Port of Paper's Suspicious-Effect-Entry-API additions on
 * {@link CraftMushroomCow}: {@code addEffectToNextStew(SuspiciousEffectEntry,
 * boolean)}, {@code getStewEffects()} and {@code setStewEffects(List)}.
 *
 * The NMS backing field {@code MushroomCow#stewEffects} is private (Paper
 * accesses it directly from within the class), so it is reached reflectively.
 */
@Mixin(CraftMushroomCow.class)
public abstract class CraftMushroomCowApiMixin {

    @Unique
    private statnecraft.world.entity.animal.MushroomCow getHandle();

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
        SuspiciousStewEffects stewEffects = paperarc$stewEffects(handle);
        if (stewEffects == null) {
            stewEffects = SuspiciousStewEffects.EMPTY;
        }
        SuspiciousStewEffects.Entry recordSuspiciousEffect =
            new SuspiciousStewEffects.Entry(minecraftPotionEffect, suspiciousEffectEntry.duration());
        this.removeEffectFromNextStew(suspiciousEffectEntry.effect()); // Avoid duplicates of effects
        paperarc$setStewEffects(handle, stewEffects.withEffectAdded(recordSuspiciousEffect));
        return true;
    }

    @Unique
    public List<io.papermc.paper.potion.SuspiciousEffectEntry> getStewEffects() {
        SuspiciousStewEffects stewEffects = paperarc$stewEffects(this.getHandle());
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
            paperarc$setStewEffects(handle, null);
            return;
        }
        List<SuspiciousStewEffects.Entry> nmsPairs = new ArrayList<>(effects.size());
        for (io.papermc.paper.potion.SuspiciousEffectEntry effect : effects) {
            nmsPairs.add(new SuspiciousStewEffects.Entry(
                CraftPotionEffectType.bukkitToMinecraftHolder(effect.effect()),
                effect.duration()));
        }
        paperarc$setStewEffects(handle, new SuspiciousStewEffects(nmsPairs));
    }

    @Unique
    private static SuspiciousStewEffects paperarc$stewEffects(net.minecraft.world.entity.animal.MushroomCow handle) {
        try {
            return (SuspiciousStewEffects) paperarc$stewEffectsField().get(handle);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS MushroomCow.stewEffects", e);
        }
    }

    @Unique
    private static void paperarc$setStewEffects(net.minecraft.world.entity.animal.MushroomCow handle,
                                                SuspiciousStewEffects effects) {
        try {
            paperarc$stewEffectsField().set(handle, effects);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS MushroomCow.stewEffects", e);
        }
    }

    @Unique
    private static Field paperarc$stewEffectsField() {
        Field field = PAPERARC$STEW_EFFECTS_FIELD;
        if (field == null) {
            synchronized (CraftMushroomCowApiMixin.class) {
                if (PAPERARC$STEW_EFFECTS_FIELD == null) {
                    try {
                        Field resolved = net.minecraft.world.entity.animal.MushroomCow.class.getDeclaredField("stewEffects");
                        resolved.setAccessible(true);
                        PAPERARC$STEW_EFFECTS_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS MushroomCow.stewEffects field not found", e);
                    }
                }
                field = PAPERARC$STEW_EFFECTS_FIELD;
            }
        }
        return field;
    }
}
