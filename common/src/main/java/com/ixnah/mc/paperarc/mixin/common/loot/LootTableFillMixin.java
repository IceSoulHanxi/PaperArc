package com.ixnah.mc.paperarc.mixin.common.loot;

import com.ixnah.mc.paperarc.bridge.craft.PaperarcLootableData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 记录"容器什么时候被战利品表填充过、被谁开的"，支撑 paper 的
 * {@code LootableInventory#hasBeenFilled/getLastFilled/hasPlayerLooted}。
 *
 * <p>锚 {@code LootTable#fill} 而不是 {@code RandomizableContainer#unpackLootTable}：
 * 后者是接口的 default 方法，方块实体侧是 {@code invokevirtual} 继承调用，注不进去；
 * 而 {@code fill} 是方块容器、储物矿车、箱船三条路径共同的唯一落点，一个注入点全覆盖。
 * 开箱玩家由 vanilla 放在 {@code THIS_ENTITY} 参数里（{@code unpackLootTable} 设的）。
 */
@Mixin(LootTable.class)
public abstract class LootTableFillMixin {

    @Inject(method = "fill(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/storage/loot/LootParams;J)V",
            at = @At("HEAD"))
    private void paperarc$recordFill(Container container, LootParams params, long seed, CallbackInfo ci) {
        Entity looter = params.contextMap().getOptional(LootContextParams.THIS_ENTITY);
        PaperarcLootableData.of(container)
                .recordFill(looter instanceof Player player ? player.getUUID() : null);
    }
}
