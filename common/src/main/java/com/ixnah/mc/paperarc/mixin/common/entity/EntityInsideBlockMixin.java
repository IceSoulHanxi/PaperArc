package com.ixnah.mc.paperarc.mixin.common.entity;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.FrogspawnBlock;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LavaCauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's EntityInsideBlockEvent.
 * <p>
 * Paper inserts {@code if (!new EntityInsideBlockEvent(...).callEvent()) return;}
 * at the top of every overridden {@code entityInside} across the 24 block classes
 * below. We replicate this with a single multi-target HEAD injection; cancelling
 * skips the whole vanilla body, exactly like Paper. Arclight only injects into
 * these methods at INVOKE/FIELD points (never HEAD), so there is no conflict.
 */
@Mixin({
        BaseFireBlock.class,
        BasePressurePlateBlock.class,
        BigDripleafBlock.class,
        BubbleColumnBlock.class,
        ButtonBlock.class,
        CactusBlock.class,
        CampfireBlock.class,
        CropBlock.class,
        DetectorRailBlock.class,
        EndGatewayBlock.class,
        EndPortalBlock.class,
        FrogspawnBlock.class,
        HoneyBlock.class,
        HopperBlock.class,
        LavaCauldronBlock.class,
        LayeredCauldronBlock.class,
        NetherPortalBlock.class,
        PitcherCropBlock.class,
        PowderSnowBlock.class,
        SweetBerryBushBlock.class,
        TripWireBlock.class,
        WaterlilyBlock.class,
        WebBlock.class,
        WitherRoseBlock.class
})
public abstract class EntityInsideBlockMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void paperarc$insideBlock(BlockState state, Level world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!new EntityInsideBlockEvent(
                PaperArcBridge.bukkitEntity(entity),
                CraftBlock.at(world, pos)).callEvent()) {
            ci.cancel();
        }
    }
}
