package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code BlockCanBuildEvent#getHand()}。
 *
 * <p>paper 把手放进构造器形参，取值来自两个触发点的 {@code context.getHand()}
 * （{@code BlockItem#canPlace} 与 {@code StandingAndWallBlockItem#getPlacementState}，
 * 见同名的两个触发点 mixin）；Arclight 的构造点是四参老签名，只能走
 * {@link EventCauseState} 传。取不到时按 paper 的默认值返回 {@code HAND}。
 */
@Mixin(BlockCanBuildEvent.class)
public abstract class BlockCanBuildEventApiMixin {

    @Unique
    private EquipmentSlot paperarc$hand = EquipmentSlot.HAND;

    @Inject(method = "<init>(Lorg/bukkit/block/Block;Lorg/bukkit/entity/Player;Lorg/bukkit/block/data/BlockData;Z)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureHand(Block block, Player player, BlockData type, boolean canBuild, CallbackInfo ci) {
        this.paperarc$hand = EventCauseState.takeBlockCanBuildHand();
    }

    @Unique
    public EquipmentSlot getHand() {
        return this.paperarc$hand;
    }
}
