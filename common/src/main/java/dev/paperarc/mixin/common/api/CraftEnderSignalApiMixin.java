package dev.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftEnderSignal;
import org.bukkit.craftbukkit.v.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.EyeOfEnder;

/**
 * Adds setTargetLocation(Location, boolean) missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Change-EnderEye-target-without-changing-other-things.patch.
 * Paper adds a 2-arg NMS signalTo; update=false is emulated by restoring
 * life/surviveAfterDeath around the vanilla single-arg signalTo.
 */
@Mixin(CraftEnderSignal.class)
public abstract class CraftEnderSignalApiMixin {

    @Shadow
    public abstract org.bukkit.World getWorld();

    @Unique
    public void setTargetLocation(Location location, boolean update) {
        Preconditions.checkArgument(this.getWorld().equals(location.getWorld()), "Cannot target EnderSignal across worlds");
        EyeOfEnder handle = ((org.bukkit.craftbukkit.v.entity.CraftEnderSignal) (Object) this).getHandle();
        BlockPos target = CraftLocation.toBlockPosition(location);
        if (update || !(handle instanceof EyeOfEnderAccessorMixin acc)) {
            handle.signalTo(target);
            return;
        }
        int life = acc.paperarc$getLife();
        boolean survive = acc.paperarc$getSurviveAfterDeath();
        handle.signalTo(target);
        acc.paperarc$setLife(life);
        acc.paperarc$setSurviveAfterDeath(survive);
    }
}
