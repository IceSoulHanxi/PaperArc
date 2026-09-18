package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.craft.PaperarcLootableData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 方块实体侧的 lootable 记账落盘（gaps.md §3.1 持久化行）。
 *
 * <p>与实体侧（{@code entity.EntityPersistenceMixin}）同一套键
 * （{@code Paper.LootableData}）；{@code saveAdditional}/{@code loadAdditional} 是所有
 * 方块实体共用的入口，只在真有记账时才写，空的方块实体一个字节都不多。
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityPersistenceMixin {

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void paperarc$saveLootable(CompoundTag nbt, HolderLookup.Provider registries, CallbackInfo ci) {
        PaperarcLootableData.saveIfPresent(this, nbt);
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void paperarc$loadLootable(CompoundTag nbt, HolderLookup.Provider registries, CallbackInfo ci) {
        PaperarcLootableData.loadIfPresent(this, nbt);
    }
}
