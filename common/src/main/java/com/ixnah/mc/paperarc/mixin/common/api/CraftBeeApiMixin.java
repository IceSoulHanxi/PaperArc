package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ApiState;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.animal.Bee;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftBee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Bee rolling/pollination API missing from Arclight's CraftBee.
 *
 * Paper stores {@code rollingOverride} as a TriState field injected into the NMS
 * Bee class; that field does not exist in the runtime NMS, so it is kept in the
 * ApiState side map keyed by the handle. {@code isRolling()}/{@code setRolling()}
 * are private in vanilla and the two pollination counters are package-private
 * fields — all widened via AT (m_27873_ / m_27929_ / f_27712_ / f_27710_) and
 * accessed directly, no reflection.
 */
@Mixin(CraftBee.class)
public abstract class CraftBeeApiMixin {

    @Unique
    private static final String PAPERARC$ROLLING_OVERRIDE_KEY = "paperarc$rollingOverride";

    @Shadow
    public abstract Bee getHandle();

    @Unique
    private TriState paperarc$getRollingOverride(Bee handle) {
        TriState stored = ApiState.get(handle, PAPERARC$ROLLING_OVERRIDE_KEY, null);
        return stored == null ? TriState.NOT_SET : stored;
    }

    @Unique
    public int getCropsGrownSincePollination() {
        return getHandle().numCropsGrownSincePollination;
    }

    @Unique
    public void setCropsGrownSincePollination(int crops) {
        getHandle().numCropsGrownSincePollination = crops;
    }

    @Unique
    public int getTicksSincePollination() {
        return getHandle().ticksWithoutNectarSinceExitingHive;
    }

    @Unique
    public void setTicksSincePollination(int ticks) {
        getHandle().ticksWithoutNectarSinceExitingHive = ticks;
    }

    @Unique
    public TriState getRollingOverride() {
        return paperarc$getRollingOverride(getHandle());
    }

    @Unique
    public void setRollingOverride(TriState rolling) {
        Bee handle = getHandle();
        TriState override = rolling == null ? TriState.NOT_SET : rolling;
        ApiState.put(handle, PAPERARC$ROLLING_OVERRIDE_KEY, override);
        // Mirror Paper's patched NMS setRolling: the roll synched-data flag becomes
        // the override value unless the override is NOT_SET (keep the current roll).
        boolean effective = override.toBooleanOrElse(handle.isRolling());
        handle.setRolling(effective);
    }

    @Unique
    public boolean isRolling() {
        return paperarc$getRollingOverride(getHandle()).toBooleanOrElse(getHandle().isRolling());
    }
}
