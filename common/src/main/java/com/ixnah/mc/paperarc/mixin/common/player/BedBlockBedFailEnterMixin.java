package com.ixnah.mc.paperarc.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerBedFailEnterEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * PlayerBedFailEnterEvent 触发点。
 * <p>
 * 对照 Paper：BedBlock#useWithoutItem 中 startSleepInBed 返回 Left（入睡失败）
 * 时发事件（FailReason 按 BedSleepingProblem.ordinal 映射，两枚举常量顺序
 * 已核对一致），取消时不爆炸、不提示消息。Paper 还把爆炸分支改为读
 * event.getWillExplode() 并允许插件改写消息；见偏差说明。
 * <p>
 * 实现：wrap useWithoutItem 内 startSleepInBed INVOKE。事件取消时返回
 * Either.Right(Unit.INSTANCE) 使 ifLeft 回调整体跳过，等价于 Paper 的
 * `if (!event.callEvent()) return;`。
 * <p>
 * 偏差（v2 TODO）：
 * <ul>
 *   <li>message 传 null：PaperAdventure.asAdventure 在本模块不可用，插件
 *       setMessage 暂不生效；非取消时原版消息逻辑不变。</li>
 *   <li>插件 setWillExplode 暂不生效：爆炸分支在运行时 ifLeft lambda 内部按
 *       dimensionType().bedWorks() 判定（CraftBukkit 补丁注入），调用点无法改写。</li>
 * </ul>
 */
@Mixin(BedBlock.class)
public abstract class BedBlockBedFailEnterMixin {

    @WrapOperation(
        method = "useWithoutItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;"
        )
    )
    private Either<Player.BedSleepingProblem, Unit> paperarc$onBedFailEnter(
            Player player, BlockPos pos,
            Operation<Either<Player.BedSleepingProblem, Unit>> original) {
        Either<Player.BedSleepingProblem, Unit> result = original.call(player, pos);
        var left = result.left();
        if (left.isEmpty()) {
            return result;
        }
        Level level = player.level();
        PlayerBedFailEnterEvent event = new PlayerBedFailEnterEvent(
            PaperArcBridge.bukkitPlayer(player),
            PlayerBedFailEnterEvent.FailReason.values()[left.get().ordinal()],
            CraftBlock.at(level, pos),
            !level.dimensionType().bedWorks(),
            null // TODO(v2): PaperAdventure.asAdventure(problem.getMessage())
        );
        if (!event.callEvent()) {
            return Either.right(Unit.INSTANCE); // 取消：跳过爆炸与消息
        }
        return result;
    }
}
