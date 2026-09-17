package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.BeeBridge;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.animal.Bee;
import org.bukkit.craftbukkit.v.entity.CraftBee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Bee rolling/pollination API missing from Arclight's CraftBee.
 *
 * Paper stores {@code rollingOverride} as a TriState field injected into the NMS
 * Bee class by {@code BeeFieldsMixin} and reached through
 * {@link com.ixnah.mc.paperarc.bridge.BeeBridge}. {@code isRolling()}/
 * {@code setRolling()} are private in vanilla and the two pollination counters
 * are package-private fields — all widened via AT (m_27873_ / m_27929_ /
 * f_27712_ / f_27710_) and accessed directly, no reflection.
 */
@Mixin(CraftBee.class)
public abstract class CraftBeeApiMixin {

    @Shadow
    public abstract Bee getHandle();

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
        return ((com.ixnah.mc.paperarc.bridge.BeeBridge) getHandle()).paper$getRollingOverride();
    }

    @Unique
    public void setRollingOverride(TriState rolling) {
        Bee handle = getHandle();
        TriState override = rolling == null ? TriState.NOT_SET : rolling;
        ((com.ixnah.mc.paperarc.bridge.BeeBridge) handle).paper$setRollingOverride(override);
        // Mirror Paper's patched NMS setRolling: the roll synched-data flag becomes
        // the override value unless the override is NOT_SET (keep the current roll).
        boolean effective = override.toBooleanOrElse(handle.isRolling());
        handle.setRolling(effective);
    }

    @Unique
    public boolean isRolling() {
        return ((com.ixnah.mc.paperarc.bridge.BeeBridge) getHandle()).paper$getRollingOverride()
                .toBooleanOrElse(getHandle().isRolling());
    }
}
