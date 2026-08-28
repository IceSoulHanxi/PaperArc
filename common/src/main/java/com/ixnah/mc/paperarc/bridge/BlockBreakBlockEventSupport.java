package com.ixnah.mc.paperarc.bridge;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared implementation of Paper's Add-BlockBreakBlockEvent.patch
 * {@code Block#dropResources(BlockState, LevelAccessor, BlockPos, BlockEntity, BlockPos)}
 * overload (the patch adds it as a static method on Block; we cannot inject
 * new call sites, so call sites are wrapped and routed through here).
 *
 * <p>Behaviour mirrors the patch verbatim: collect vanilla drops, fire
 * {@link BlockBreakBlockEvent} with (block, source, drops), then pop the
 * (possibly plugin-modified) event drops instead of the vanilla ones and run
 * {@code spawnAfterBreak}.
 */
public final class BlockBreakBlockEventSupport {

    private BlockBreakBlockEventSupport() {
    }

    public static void breakWithEvent(BlockState state, LevelAccessor accessor, BlockPos pos,
                                      BlockEntity blockEntity, BlockPos source) {
        if (!(accessor instanceof ServerLevel serverLevel)) {
            Block.dropResources(state, accessor, pos, blockEntity);
            return;
        }
        List<org.bukkit.inventory.ItemStack> items = new ArrayList<>();
        for (ItemStack drop : Block.getDrops(state, serverLevel, pos, blockEntity)) {
            items.add(CraftItemStack.asBukkitCopy(drop));
        }
        BlockBreakBlockEvent event = new BlockBreakBlockEvent(
                CraftBlock.at(accessor, pos),
                CraftBlock.at(accessor, source),
                items);
        event.callEvent();
        for (org.bukkit.inventory.ItemStack bukkitDrop : event.getDrops()) {
            Block.popResource(serverLevel, pos, CraftItemStack.asNMSCopy(bukkitDrop));
        }
        state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
    }
}
