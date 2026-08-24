package com.ixnah.mc.paperarc.mixin.common.block;

import net.minecraft.world.LockCode;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Duck accessor for BaseContainerBlockEntity's private lockKey, needed by
 * BeaconBlockEntityMixin (subclass mixins must not @Shadow parent fields).
 */
@Mixin(BaseContainerBlockEntity.class)
public interface LockCodeAccessor {

    @Accessor("lockKey")
    LockCode paperarc$getLockKey();
}
