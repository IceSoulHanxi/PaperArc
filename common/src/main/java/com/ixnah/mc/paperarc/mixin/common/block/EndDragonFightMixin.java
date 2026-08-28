package com.ixnah.mc.paperarc.mixin.common.block;

import io.papermc.paper.event.block.DragonEggFormEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_20_R1.boss.CraftDragonBattle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's DragonEggFormEvent
 * (Add-DragonEggFormEvent.patch).
 *
 * Paper replaces the direct DRAGON_EGG placement in
 * EndDragonFight#setDragonKilled with a cancellable DragonEggFormEvent whose
 * new state is applied via BlockState#update(true). We inject cancellable at
 * the setBlockAndUpdate call and replicate that flow unconditionally (cancel
 * covers both the event-denied and already-previously-killed cases).
 *
 * Deviation: Paper's config toggle
 * entities.behavior.enderDragonsDeathAlwaysPlacesDragonEgg has no equivalent
 * here — egg is placed only when !previouslyKilled (vanilla default).
 */
@Mixin(EndDragonFight.class)
public abstract class EndDragonFightMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Final
    private BlockPos origin;

    @Shadow
    private boolean previouslyKilled;

    @Inject(method = "setDragonKilled",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"),
            cancellable = true)
    private void paperarc$dragonEggForm(EnderDragon dragon, CallbackInfo ci) {
        if (!this.previouslyKilled) {
            BlockPos eggPosition = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                    EndPodiumFeature.getLocation(this.origin));
            CraftBlockState eggState = CraftBlockStates.getBlockState(this.level, eggPosition);
            eggState.setData(Blocks.DRAGON_EGG.defaultBlockState());
            DragonEggFormEvent event = new DragonEggFormEvent(CraftBlock.at(this.level, eggPosition), eggState,
                    new CraftDragonBattle((EndDragonFight) (Object) this));
            if (event.callEvent()) {
                event.getNewState().update(true);
            }
        }
        ci.cancel();
    }
}
