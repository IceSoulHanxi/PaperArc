package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.reflect.Method;
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
 * <p>{@code getInteractableSideFor} 原先反射 {@code SignBlockEntity#isFacingFrontText(double,
 * double)} —— 那是 Paper 自己加的重载，Arclight 根本没有，一调就 {@code IllegalStateException}
 * （checklist §1.9 ag，main 已修）。改为照抄 vanilla {@code isFacingFrontText(Player)} 的
 * 夹角计算，放 {@code bridge/api/PaperarcSignFacing}。
 */
@Mixin(CraftSign.class)
public abstract class CraftSignApiMixin {

    @Unique
    private static Method paperarc$tileEntityMethod;

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
        boolean front = com.ixnah.mc.paperarc.bridge.api.PaperarcSignFacing
                .isFacingFrontText(this.paperarc$signBlockEntity(), x, z);
        return front ? Side.FRONT : Side.BACK;
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

    // 反射目标是 CraftBukkit 侧成员（不参与 srg 重映射），按字面名查找在运行时正确；
    // NMS 成员必须改走 AT/@Accessor —— 见 docs/execution-plan-2026-09-16.md A2。
    @Unique
    private SignBlockEntity paperarc$signBlockEntity() {
        try {
            if (paperarc$tileEntityMethod == null) {
                Method method = Class.forName("org.bukkit.craftbukkit.v.block.CraftBlockEntityState")
                    .getDeclaredMethod("getTileEntity"); // protected in CraftBlockEntityState
                method.setAccessible(true);
                paperarc$tileEntityMethod = method;
            }
            BlockEntity blockEntity = (BlockEntity) paperarc$tileEntityMethod.invoke(this);
            if (!(blockEntity instanceof SignBlockEntity sign)) {
                throw new IllegalStateException("PaperArc: tile entity is not a SignBlockEntity: " + blockEntity);
            }
            return sign;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: cannot access CraftBlockEntityState#getTileEntity()", e);
        }
    }
}
