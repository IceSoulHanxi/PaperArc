package dev.paperarc.mixin.common.api;

import java.lang.reflect.Method;

import org.bukkit.craftbukkit.v.block.CraftCampfire;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.google.common.base.Preconditions;

import dev.paperarc.bridge.ApiState;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

/**
 * Adds Paper's "Add more Campfire API" additions to CraftCampfire.
 *
 * Paper ref: patches/server/Add-more-Campfire-API.patch.
 *
 * Paper adds a {@code public final boolean[] stopCooking} field to NMS
 * CampfireBlockEntity (persisted via an extra NBT byte array and honored inside
 * cookTick). Vanilla mojmap NMS has no such field, so the per-slot flags are kept in
 * the ApiState side-map keyed by the snapshot BlockEntity instance ("side-map"):
 * state survives as long as the snapshot object does, but is not persisted to disk
 * and vanilla cookTick does not consume it.
 *
 * {@code CraftBlockEntityState#getSnapshot()} is protected (subclass-target mixins
 * cannot shadow inherited members), so it is reached reflectively, matching the
 * established CraftFurnaceApiMixin pattern.
 */
@Mixin(CraftCampfire.class)
public abstract class CraftCampfireApiMixin {

    @Unique
    private static final String PAPERARC$SNAPSHOT_OWNER = "org.bukkit.craftbukkit.v.block.CraftBlockEntityState";

    @Unique
    private static volatile Method PAPERARC$SNAPSHOT_METHOD;

    @Unique
    private static Method paperarc$snapshotMethod() {
        Method m = PAPERARC$SNAPSHOT_METHOD;
        if (m == null) {
            synchronized (CraftCampfireApiMixin.class) {
                if (PAPERARC$SNAPSHOT_METHOD == null) {
                    try {
                        Method resolved = Class.forName(PAPERARC$SNAPSHOT_OWNER).getDeclaredMethod("getSnapshot");
                        resolved.setAccessible(true);
                        PAPERARC$SNAPSHOT_METHOD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("PaperArc: cannot access CraftBlockEntityState#getSnapshot()", e);
                    }
                }
                m = PAPERARC$SNAPSHOT_METHOD;
            }
        }
        return m;
    }

    @Unique
    private CampfireBlockEntity paperarc$snapshot() {
        try {
            return (CampfireBlockEntity) paperarc$snapshotMethod().invoke(this);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: failed to read campfire snapshot", e);
        }
    }

    @Unique
    private static String paperarc$flagKey(int index) {
        return "paperarc.stopCooking." + index;
    }

    @Unique
    public void stopCooking() {
        for (int i = 0; i < 4; ++i) {
            this.stopCooking(i);
        }
    }

    @Unique
    public void startCooking() {
        for (int i = 0; i < 4; ++i) {
            this.startCooking(i);
        }
    }

    @Unique
    public boolean stopCooking(int index) {
        Preconditions.checkArgument(-1 < index && index < 4, "Slot index must be between 0 (incl) to 3 (incl)");
        boolean previous = this.isCookingDisabled(index);
        ApiState.put(this.paperarc$snapshot(), paperarc$flagKey(index), true); // side-map
        return previous;
    }

    @Unique
    public boolean startCooking(int index) {
        Preconditions.checkArgument(-1 < index && index < 4, "Slot index must be between 0 (incl) to 3 (incl)");
        boolean previous = this.isCookingDisabled(index);
        ApiState.put(this.paperarc$snapshot(), paperarc$flagKey(index), false); // side-map
        return previous;
    }

    @Unique
    public boolean isCookingDisabled(int index) {
        Preconditions.checkArgument(-1 < index && index < 4, "Slot index must be between 0 (incl) to 3 (incl)");
        return Boolean.TRUE.equals(ApiState.get(this.paperarc$snapshot(), paperarc$flagKey(index), Boolean.FALSE));
    }
}
