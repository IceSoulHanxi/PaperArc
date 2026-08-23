package dev.paperarc.bridge;

import io.papermc.paper.block.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Fluid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v.CraftFluid;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.util.Vector;

/**
 * Minimal server-side implementation of paper-api {@link FluidData}.
 *
 * Paper ships this as its internal PaperFluidData (paper-server only); since
 * paperarc shades only the API jar, we provide an equivalent backed by the
 * NMS FluidState. Mirrors Paper semantics:
 * getFluidType -> CraftFluid.minecraftToBukkit(state.getType()),
 * computeFlowDirection/computeHeight delegate to the NMS flow model.
 */
public class PaperArcFluidData implements FluidData {

    private final FluidState state;

    public PaperArcFluidData(FluidState state) {
        this.state = state;
    }

    @Override
    public Fluid getFluidType() {
        return CraftFluid.minecraftToBukkit(this.state.getType());
    }

    @Override
    public Vector computeFlowDirection(Location location) {
        Vec3 flow = this.state.getFlow(blockGetter(location.getWorld()),
            new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return new Vector(flow.x, flow.y, flow.z);
    }

    @Override
    public int getLevel() {
        return this.state.getAmount();
    }

    @Override
    public float computeHeight(Location location) {
        return this.state.getHeight(blockGetter(location.getWorld()),
            new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    @Override
    public boolean isSource() {
        return this.state.isSource();
    }

    @Override
    public FluidData clone() {
        try {
            return (FluidData) super.clone();
        } catch (CloneNotSupportedException e) {
            return new PaperArcFluidData(this.state);
        }
    }

    private static BlockGetter blockGetter(World world) {
        return ((CraftWorld) world).getHandle();
    }
}
