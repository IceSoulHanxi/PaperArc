package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Preconditions;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.craftbukkit.v.block.CraftBlockState;
import org.bukkit.craftbukkit.v.block.CraftSign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Ports of Paper's Adventure.patch and More-Sign-Block-API.patch additions on
 * {@link CraftSign}: adventure {@code line}/{@code lines}, editor UUID access
 * and {@code getInteractableSideFor(double, double)}.
 *
 * <p>Adventure methods delegate to the front {@link SignSide} exactly like
 * Paper's implementation delegates to {@code this.front}; at runtime Paper's
 * CraftSignSide provides the Component overloads declared by paper-api.
 * <p>{@code getInteractableSideFor} 原先反射找 {@code SignBlockEntity#isFacingFrontText(double,
 * double)} —— 那是 <b>Paper 自己加的重载</b>，Arclight 运行时压根没有，整条路径一调就
 * {@code IllegalStateException}（B3-3 探针 P20 实测；同 checklist §1.6 o 那一类）。
 * 现在改为在 {@code bridge/api/PaperarcSignFacing} 里照抄 vanilla 的那段夹角计算。
 */
@Mixin(CraftSign.class)
public abstract class CraftSignApiMixin {

    @Shadow
    public abstract SignSide getSide(Side side);

    // Paper start - Adventure

    @Unique
    public List<net.kyori.adventure.text.Component> lines() {
        return this.getSide(Side.FRONT).lines();
    }

    @Unique
    public net.kyori.adventure.text.Component line(int index) throws IndexOutOfBoundsException {
        return this.getSide(Side.FRONT).line(index);
    }

    @Unique
    public void line(int index, net.kyori.adventure.text.Component line) throws IndexOutOfBoundsException {
        this.getSide(Side.FRONT).line(index, line);
    }

    // Paper start - More Sign Block API

    @Unique
    public UUID getAllowedEditorUniqueId() {
        this.paperarc$ensureNoWorldGeneration();
        return this.paperarc$signBlockEntity().getPlayerWhoMayEdit();
    }

    @Unique
    public void setAllowedEditorUniqueId(UUID uuid) {
        this.paperarc$ensureNoWorldGeneration();
        this.paperarc$signBlockEntity().setAllowedPlayerEditor(uuid);
    }

    @Unique
    public Side getInteractableSideFor(final double x, final double z) {
        Preconditions.checkState(((CraftBlockState) (Object) this).isPlaced(), "The blockState must be placed");
        return com.ixnah.mc.paperarc.bridge.api.PaperarcSignFacing
                .isFacingFrontText(this.paperarc$signBlockEntity(), x, z) ? Side.FRONT : Side.BACK;
    }

    /**
     * Mirror of the protected {@code CraftBlockState#ensureNoWorldGeneration()}
     * guard used by Paper's editor accessors.
     */
    @Unique
    private void paperarc$ensureNoWorldGeneration() {
        LevelAccessor worldHandle = ((CraftBlockState) (Object) this).getWorldHandle();
        if (worldHandle instanceof WorldGenRegion) {
            throw new IllegalStateException("Cannot interact with a sign during world generation");
        }
    }

    @Unique
    private SignBlockEntity paperarc$signBlockEntity() {
        // CraftBlockEntityState#getTileEntity() 是 protected，走 provider bridge
        BlockEntity blockEntity =
            ((com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge) (Object) this).paperarc$getTileEntity();
        if (!(blockEntity instanceof SignBlockEntity sign)) {
            throw new IllegalStateException("PaperArc: tile entity is not a SignBlockEntity: " + blockEntity);
        }
        return sign;
    }

}
