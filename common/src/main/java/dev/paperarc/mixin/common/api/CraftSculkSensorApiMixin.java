package dev.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import dev.paperarc.bridge.ApiState;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import org.bukkit.block.data.type.SculkSensor;
import org.bukkit.craftbukkit.v.block.CraftSculkSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Configurable-sculk-sensor-listener-range.patch additions on
 * {@link CraftSculkSensor}: {@code get/setListenerRange()} plus the
 * {@code get/setPhase(Phase)} accessors.
 *
 * <p>Vanilla NMS has no per-sensor range-override field (that is a
 * Paper-side addition), so the range lives in the ApiState side map keyed by
 * the Craft state instance; unset reads fall back to the vanilla listener
 * radius from the snapshot's vibration user.</p>
 */
@Mixin(CraftSculkSensor.class)
public abstract class CraftSculkSensorApiMixin {

    @Unique
    private static final String PAPERARC_LISTENER_RANGE_KEY = "paperarc:listenerRange";

    @Shadow
    protected abstract SculkSensorBlockEntity getSnapshot();

    @Shadow
    public abstract org.bukkit.block.data.BlockData getBlockData();

    @Shadow
    public abstract void setBlockData(org.bukkit.block.data.BlockData blockData);

    // Paper start - Configurable sculk sensor listener range
    @Unique
    public int getListenerRange() {
        Integer override = ApiState.get(this, PAPERARC_LISTENER_RANGE_KEY, null);
        if (override != null) {
            return override;
        }
        return this.getSnapshot().getListener().getListenerRadius();
    }

    @Unique
    public void setListenerRange(int range) {
        Preconditions.checkArgument(range > 0, "Vibration listener range must be greater than 0");
        ApiState.put(this, PAPERARC_LISTENER_RANGE_KEY, range);
    }
    // Paper end - Configurable sculk sensor listener range

    @Unique
    public SculkSensor.Phase getPhase() {
        return ((SculkSensor) this.getBlockData()).getPhase();
    }

    @Unique
    public void setPhase(SculkSensor.Phase phase) {
        SculkSensor blockData = (SculkSensor) this.getBlockData();
        blockData.setPhase(phase);
        this.setBlockData(blockData);
    }
}
