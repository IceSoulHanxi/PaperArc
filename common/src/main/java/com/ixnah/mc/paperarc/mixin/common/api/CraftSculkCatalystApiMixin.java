package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.ApiState;
import io.papermc.paper.math.Position;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftSculkCatalyst;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Method;

/**
 * Port of Paper's SculkCatalyst-bloom-API additions on
 * {@link CraftSculkCatalyst}: {@code bloom(Position, int)} plus the
 * {@code isBloom()}/{@code setBloom(boolean)} accessors declared by
 * paper-api (no vanilla per-catalyst storage exists, so those two use the
 * ApiState side map).
 */
@Mixin(CraftSculkCatalyst.class)
public abstract class CraftSculkCatalystApiMixin {

    @Unique
    private static final String PAPERARC_BLOOM_KEY = "paperarc:bloom";

    @Unique
    private static volatile Method paperarc$catalystBloom;

    @Unique
    private boolean isPlaced() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$isPlaced();
    }

    @Unique
    private org.bukkit.World getWorld() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$getWorld();
    }

    @Unique
    private net.minecraft.world.level.block.entity.BlockEntity getTileEntityFromWorld() {
        return ((CraftBlockEntityStateBridge) (Object) this).paperarc$getTileEntityFromWorld();
    }

    @Unique
    public void bloom(Position position, int charge) {
        Preconditions.checkNotNull(position, "position cannot be null");
        // Paper calls requirePlaced(); equivalent guard via public isPlaced()
        Preconditions.checkState(this.isPlaced(), "Cannot bloom an unplaced state");
        ServerLevel level = ((org.bukkit.craftbukkit.v1_20_R1.CraftWorld) this.getWorld()).getHandle();
        SculkCatalystBlockEntity catalyst = (SculkCatalystBlockEntity) this.getTileEntityFromWorld();
        try {
            Method bloom = paperarc$catalystBloomMethod();
            bloom.invoke(catalyst.getListener(), level, catalyst.getBlockPos(), catalyst.getBlockState(), level.getRandom());
            catalyst.getListener().getSculkSpreader().addCursors(
                BlockPos.containing(position.x(), position.y(), position.z()), charge);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to invoke CatalystListener#bloom", e);
        }
    }

    @Unique
    public boolean isBloom() {
        return ApiState.get(this, PAPERARC_BLOOM_KEY, Boolean.FALSE);
    }

    @Unique
    public void setBloom(boolean bloom) {
        ApiState.put(this, PAPERARC_BLOOM_KEY, bloom);
    }

    /**
     * Vanilla {@code CatalystListener#bloom(ServerLevel, BlockPos, BlockState,
     * RandomSource)} is private; Paper widens it via AT, we resolve it once
     * reflectively (Arclight runs mojmap at runtime so the name is stable).
     */
    @Unique
    private static Method paperarc$catalystBloomMethod() throws NoSuchMethodException {
        Method method = paperarc$catalystBloom;
        if (method == null) {
            synchronized (CraftSculkCatalystApiMixin.class) {
                if (paperarc$catalystBloom == null) {
                    Method declared = SculkCatalystBlockEntity.CatalystListener.class.getDeclaredMethod("bloom",
                        ServerLevel.class, BlockPos.class,
                        net.minecraft.world.level.block.state.BlockState.class, RandomSource.class);
                    declared.setAccessible(true);
                    paperarc$catalystBloom = declared;
                }
                method = paperarc$catalystBloom;
            }
        }
        return method;
    }
}
