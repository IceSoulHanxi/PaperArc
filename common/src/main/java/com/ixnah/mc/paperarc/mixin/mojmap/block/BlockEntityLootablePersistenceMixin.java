package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.ixnah.mc.paperarc.bridge.PaperarcLootableData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code LootableBlockInventory} 的"谁看过这个容器"记账落盘（A8/Y-3）。
 *
 * <p>挂在 {@code BlockEntity} 而不是各个箱子类上：{@code saveWithoutMetadata()} 是
 * 所有方块实体存档的唯一汇聚点（{@code saveWithId}/{@code saveWithFullMetadata} 都调它，
 * `javap -p` 核对），{@code load} 则被各子类 {@code super.load(nbt)} 汇过来。
 * 没有记账的方块实体不会多写任何标签（见 {@link PaperarcLootableData#saveIfPresent}）。
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityLootablePersistenceMixin {

    @Inject(method = "saveWithoutMetadata", at = @At("RETURN"))
    private void paperarc$saveLootableData(CallbackInfoReturnable<CompoundTag> cir) {
        PaperarcLootableData.saveIfPresent(this, cir.getReturnValue());
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void paperarc$loadLootableData(CompoundTag nbt, CallbackInfo ci) {
        PaperarcLootableData.loadIfPresent(this, nbt);
    }
}
