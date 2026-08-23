package dev.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import io.papermc.paper.block.fluid.FluidData;
import io.papermc.paper.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v.block.CraftBiome;
import org.bukkit.craftbukkit.v.CraftRegionAccessor;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.util.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's additions on {@link CraftRegionAccessor}:
 * {@code getComputedBiome(int,int,int)} (Implement-getComputedBiome-API),
 * {@code getFluidData(int,int,int)} (Add-FluidState-API),
 * {@code getKey()} (Expand-world-key-API),
 * {@code getMoonPhase()} (Add-moon-phase-API),
 * {@code hasCollisionsIn(BoundingBox)} (Collision-API) and
 * {@code lineOfSightExists(Location,Location)} (Line-Of-Sight-Changes).
 */
@Mixin(CraftRegionAccessor.class)
public abstract class CraftRegionAccessorApiMixin {

    @Shadow
    public abstract WorldGenLevel getHandle();

    @Unique
    public Biome getComputedBiome(int x, int y, int z) {
        // Paper: CraftBiome.minecraftHolderToBukkit(this.getHandle().getBiome(new BlockPos(x, y, z)))
        return CraftBiome.minecraftHolderToBukkit(this.getHandle().getBiome(new BlockPos(x, y, z)));
    }

    @Unique
    public io.papermc.paper.block.fluid.FluidData getFluidData(int x, int y, int z) {
        // Paper: new PaperFluidData(getHandle().getFluidState(new BlockPos(x, y, z))) — paperarc 用 bridge 实现等价类
        return new dev.paperarc.bridge.PaperArcFluidData(this.getHandle().getFluidState(new BlockPos(x, y, z)));
    }

    @Unique
    public NamespacedKey getKey() {
        // Paper: CraftNamespacedKey.fromMinecraft(this.getHandle().getLevel().dimension().location())
        return CraftNamespacedKey.fromMinecraft(this.getHandle().getLevel().dimension().location());
    }

    @Unique
    public MoonPhase getMoonPhase() {
        // Paper: MoonPhase.getPhase(this.getHandle().dayTime() / 24000L); day time getter is
        // Level#getDayTime in these mappings, reached through WorldGenLevel#getLevel()
        return MoonPhase.getPhase(this.getHandle().getLevel().getDayTime() / 24000L);
    }

    @Unique
    public boolean hasCollisionsIn(BoundingBox boundingBox) {
        AABB aabb = new AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        return !this.getHandle().noCollision(aabb);
    }

    @Unique
    public boolean lineOfSightExists(Location from, Location to) {
        Preconditions.checkArgument(from != null, "from parameter in lineOfSightExists cannot be null");
        Preconditions.checkArgument(to != null, "to parameter in lineOfSightExists cannot be null");
        if (from.getWorld() != to.getWorld()) {
            return false;
        }
        Vec3 start = new Vec3(from.getX(), from.getY(), from.getZ());
        Vec3 end = new Vec3(to.getX(), to.getY(), to.getZ());
        return this.getHandle().clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()))
                .getType() == HitResult.Type.MISS;
    }
}
