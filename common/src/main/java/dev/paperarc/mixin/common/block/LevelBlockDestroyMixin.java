package dev.paperarc.mixin.common.block;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's BlockDestroyEvent
 * (BlockDestroyEvent.patch).
 *
 * Paper fires the event in Level#destroyBlock(BlockPos, boolean, Entity, int)
 * right after the air check, just before the fluid state is read. We inject at
 * the INVOKE of getFluidState (equivalent position) and cancel with a
 * {@code false} return when the event is cancelled.
 *
 * Deviation from Paper: the exp value passed to the event is always 0 — Paper
 * computes it via its own Block#getExpDrop patch, which does not exist in
 * vanilla 1.21.1 (only protected tryDropExperience), and vanilla never uses
 * the value downstream anyway.
 *
 * Plugin modifications to playEffect / effectBlock / willDrop are honored by
 * wrapping the two downstream vanilla callsites (levelEvent 2001 effect and
 * Block#dropResources). The event is only fired when listeners are registered
 * (Paper does the same); a null sentinel effectType marks "event not fired" so
 * the wrappers fall through to vanilla behavior.
 */
@Mixin(Level.class)
public abstract class LevelBlockDestroyMixin {

    @Unique
    private static final String PAPERARC$DESTROY_BLOCK
            = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z";

    @Unique
    private BlockState paperarc$effectType;

    @Unique
    private boolean paperarc$playEffect = true;

    @Unique
    private boolean paperarc$willDrop = true;

    @Inject(method = PAPERARC$DESTROY_BLOCK, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"),
            cancellable = true)
    private void paperarc$blockDestroy(BlockPos pos, boolean drop, Entity entity, int limit,
                                       CallbackInfoReturnable<Boolean> cir) {
        // Paper skips all work when no plugin listens.
        if (BlockDestroyEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        // destroyBlock is also reachable client-side (ClientLevel); event is server-only.
        if (!((Object) this instanceof ServerLevel serverLevel)) {
            return;
        }
        Level level = (Level) (Object) this;
        BlockState state = level.getBlockState(pos);
        FluidState fluid = level.getFluidState(pos);
        BlockState effectType = state;

        BlockDestroyEvent event = new BlockDestroyEvent(
                CraftBlock.at(level, pos),
                CraftBlockData.fromData(fluid.createLegacyBlock()),
                CraftBlockData.fromData(effectType),
                0, // exp: see class javadoc
                drop);
        if (!event.callEvent()) {
            cir.setReturnValue(false);
            return;
        }
        this.paperarc$effectType = ((CraftBlockData) event.getEffectBlock()).getState();
        this.paperarc$playEffect = event.playEffect();
        this.paperarc$willDrop = event.willDrop();
    }

    @WrapOperation(method = PAPERARC$DESTROY_BLOCK, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    private void paperarc$destroyEffect(int type, BlockPos pos, int data, Operation<Void> original) {
        if (this.paperarc$effectType == null) {
            // Event was not fired (no listeners) — vanilla behavior.
            original.call(type, pos, data);
            return;
        }
        // Paper: if (playEffect && !(effectType.getBlock() instanceof BaseFireBlock))
        //   this.levelEvent(2001, pos, Block.getId(effectType));
        if (!this.paperarc$playEffect || this.paperarc$effectType.getBlock() instanceof BaseFireBlock) {
            return;
        }
        original.call(type, pos, Block.getId(this.paperarc$effectType));
    }

    @WrapOperation(method = PAPERARC$DESTROY_BLOCK, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V"))
    private void paperarc$destroyDrops(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity,
                                       Entity entity, ItemStack tool, Operation<Void> original) {
        if (this.paperarc$effectType != null && !this.paperarc$willDrop) {
            return;
        }
        original.call(state, level, pos, blockEntity, entity, tool);
    }
}
