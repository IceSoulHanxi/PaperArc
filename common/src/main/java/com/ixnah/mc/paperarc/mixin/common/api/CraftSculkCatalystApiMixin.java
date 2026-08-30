package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import io.papermc.paper.math.Position;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftSculkCatalyst;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's SculkCatalyst-bloom-API additions on
 * {@link CraftSculkCatalyst}: {@code bloom(Position, int)} plus the
 * {@code isBloom()}/{@code setBloom(boolean)} accessors declared by
 * paper-api (no vanilla per-catalyst storage exists, so those two use the
 * ApiState side map).
 *
 * <p>Paper's NMS-side {@code CatalystListener#bloom(...)} is a Paper patch
 * method that does not exist in the vanilla runtime jar, so it cannot be AT'd;
 * {@code bloom(Position, int)} degrades to spreading sculk cursors only (the
 * vanilla-visible effect of a catalyst bloom), and the reflectively-resolved
 * private call is dropped — no reflection.</p>
 */
@Mixin(CraftSculkCatalyst.class)
public abstract class CraftSculkCatalystApiMixin {

    // Paper 无 per-catalyst bloom 存储（side addition）；注入 Craft 字段。
    @Unique
    private boolean bloom;

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
        // Paper 的 CatalystListener#bloom 是 Paper 补丁私有方法，vanilla 运行时无——
        // 降级为只播撒 sculk 光标（催化绽放的可见效果）。
        catalyst.getListener().getSculkSpreader().addCursors(
            BlockPos.containing(position.x(), position.y(), position.z()), charge);
    }

    @Unique
    public boolean isBloom() {
        return this.bloom;
    }

    @Unique
    public void setBloom(boolean bloom) {
        this.bloom = bloom;
    }
}
