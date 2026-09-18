package com.ixnah.mc.paperarc.bridge.api;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * {@code Sign#getInteractableSideFor} 要用的"站在哪一侧"判定。
 *
 * <p>Paper 是在 NMS {@code SignBlockEntity} 上加了一个
 * {@code isFacingFrontText(double, double)} 重载（vanilla 只有收 Player 的那个）。
 * Arclight 没打这个补丁，所以照抄 vanilla {@code isFacingFrontText(Player)} 的方法体，
 * 把取坐标那一步换成传入的 x/z —— 与 Paper 的重载逐行等价。
 */
public final class PaperarcSignFacing {

    private PaperarcSignFacing() {
    }

    public static boolean isFacingFrontText(SignBlockEntity sign, double x, double z) {
        BlockState state = sign.getBlockState();
        if (!(state.getBlock() instanceof SignBlock signBlock)) {
            return true;
        }
        Vec3 center = signBlock.getSignHitboxCenterPosition(state);
        double dx = x - ((double) sign.getBlockPos().getX() + center.x);
        double dz = z - ((double) sign.getBlockPos().getZ() + center.z);
        float facing = signBlock.getYRotationDegrees(state);
        float toViewer = (float) (Mth.atan2(dz, dx) * 57.2957763671875D) - 90.0F;
        return Mth.degreesDifferenceAbs(facing, toViewer) <= 90.0F;
    }
}
