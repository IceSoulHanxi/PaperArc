package dev.paperarc.bridge.craft;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Duck-typing bridge merged onto {@code CraftBlockEntityState} by its
 * provider mixin. Generic snapshot accessors erase to BlockEntity at
 * runtime; consumers narrow the result themselves.
 */
public interface CraftBlockEntityStateBridge extends CraftBlockStateBridge {

    BlockEntity paperarc$getSnapshot();

    BlockEntity paperarc$getTileEntityFromWorld();
}
