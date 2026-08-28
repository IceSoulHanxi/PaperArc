package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.animal.Dolphin;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftDolphin;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Missing-Entity-API (Missing Dolphin API) additions on
 * {@link CraftDolphin}.
 *
 * Moistness lives only in the entity data layer behind the private static
 * accessor {@code Dolphin.MOISTNESS_LEVEL} (vanilla 1.21.1 has no public
 * setter), so {@link #setMoistness(int)} writes through it reflectively;
 * everything else maps to public NMS methods.
 */
@Mixin(CraftDolphin.class)
public abstract class CraftDolphinApiMixin {

    @Shadow
    public abstract Dolphin getHandle();

    @Unique
    private static volatile EntityDataAccessor<Integer> PAPERARC$MOISTNESS_ACCESSOR;

    @SuppressWarnings("unchecked")
    @Unique
    private static EntityDataAccessor<Integer> paperarc$moistnessAccessor() {
        EntityDataAccessor<Integer> acc = PAPERARC$MOISTNESS_ACCESSOR;
        if (acc == null) {
            synchronized (CraftDolphinApiMixin.class) {
                if (PAPERARC$MOISTNESS_ACCESSOR == null) {
                    try {
                        java.lang.reflect.Field f = Dolphin.class.getDeclaredField("MOISTNESS_LEVEL");
                        f.setAccessible(true);
                        PAPERARC$MOISTNESS_ACCESSOR = (EntityDataAccessor<Integer>) f.get(null);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Dolphin.MOISTNESS_LEVEL accessor not found", e);
                    }
                }
                acc = PAPERARC$MOISTNESS_ACCESSOR;
            }
        }
        return acc;
    }

    // Paper start - Missing Dolphin API
    @Unique
    public int getMoistness() {
        return this.getHandle().getMoistnessLevel();
    }

    @Unique
    public void setMoistness(int moistness) {
        this.getHandle().getEntityData().set(paperarc$moistnessAccessor(), moistness);
    }

    @Unique
    public boolean hasFish() {
        return this.getHandle().gotFish();
    }

    @Unique
    public void setHasFish(boolean hasFish) {
        this.getHandle().setGotFish(hasFish);
    }

    @Unique
    public Location getTreasureLocation() {
        BlockPos pos = this.getHandle().getTreasurePos();
        return pos == null ? null : CraftLocation.toBukkit(pos, this.getHandle().level());
    }

    @Unique
    public void setTreasureLocation(Location location) {
        this.getHandle().setTreasurePos(CraftLocation.toBlockPosition(location));
    }
    // Paper end - Missing Dolphin API
}
