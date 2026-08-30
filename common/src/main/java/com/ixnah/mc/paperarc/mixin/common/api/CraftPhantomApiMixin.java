package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PhantomBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Phantom;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPhantom;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's phantom APIs to {@link CraftPhantom}.
 *
 * <p>{@code Phantom.anchorPoint} is a package-private vanilla field with no
 * accessor, so it is widened via AT (f_33098_) and accessed directly — no
 * reflection.</p>
 *
 * <p>{@code shouldBurnInDay} and {@code spawningEntity} are fields added by
 * Paper's server patches; they are injected into the NMS {@code Phantom} by
 * {@code PhantomFieldsMixin} and reached through
 * {@link com.ixnah.mc.paperarc.bridge.PhantomBridge} (defaults match Paper:
 * {@code shouldBurnInDay=true}, {@code spawningEntity=null}).</p>
 */
@Mixin(CraftPhantom.class)
public abstract class CraftPhantomApiMixin {

    @Shadow
    public abstract Phantom getHandle();

    @Unique
    public java.util.UUID getSpawningEntity() {
        return ((com.ixnah.mc.paperarc.bridge.PhantomBridge) getHandle()).paper$getSpawningEntity();
    }

    @Unique
    public boolean shouldBurnInDay() {
        return ((com.ixnah.mc.paperarc.bridge.PhantomBridge) getHandle()).shouldBurnInDay();
    }

    @Unique
    public void setShouldBurnInDay(boolean shouldBurnInDay) {
        ((com.ixnah.mc.paperarc.bridge.PhantomBridge) getHandle()).setShouldBurnInDay(shouldBurnInDay);
    }

    @Unique
    public Location getAnchorLocation() {
        return CraftLocation.toBukkit(getHandle().anchorPoint, getHandle().level());
    }

    @Unique
    public void setAnchorLocation(Location location) {
        com.google.common.base.Preconditions.checkArgument(location != null, "location cannot be null");
        getHandle().anchorPoint = CraftLocation.toBlockPosition(location);
    }
}
