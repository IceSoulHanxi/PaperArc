package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import io.papermc.paper.math.Position;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftSculkCatalyst;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's SculkCatalyst-bloom-API additions on
 * {@link CraftSculkCatalyst}: {@code bloom(Position, int)} plus the
 * paper-api 的 {@code SculkCatalyst} 只有两个 {@code bloom(...)} 重载
 * （javap 核对），没有 isBloom/setBloom，所以这里不需要任何状态字段。
 */
@Mixin(CraftSculkCatalyst.class)
public abstract class CraftSculkCatalystApiMixin {

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
        ServerLevel level = ((org.bukkit.craftbukkit.v.CraftWorld) this.getWorld()).getHandle();
        SculkCatalystBlockEntity catalyst = (SculkCatalystBlockEntity) this.getTileEntityFromWorld();
        // 原版 CatalystListener#bloom 是 private，由 paperarc.accesswidener 放开
        catalyst.getListener().bloom(level, catalyst.getBlockPos(), catalyst.getBlockState(), level.getRandom());
        catalyst.getListener().getSculkSpreader().addCursors(
            BlockPos.containing(position.x(), position.y(), position.z()), charge);
    }

}
