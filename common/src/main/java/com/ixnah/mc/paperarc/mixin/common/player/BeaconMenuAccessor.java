package com.ixnah.mc.paperarc.mixin.common.player;

import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * BeaconMenu 私有字段访问器：供 BeaconEffect 事件 handler 重演
 * updateEffects 方法体（Paper 语义需要读 paymentSlot/beaconData/access）。
 */
@Mixin(BeaconMenu.class)
public interface BeaconMenuAccessor {

    @Accessor("beaconData")
    ContainerData paperarc$getBeaconData();

    @Accessor("access")
    ContainerLevelAccess paperarc$getAccess();
}
