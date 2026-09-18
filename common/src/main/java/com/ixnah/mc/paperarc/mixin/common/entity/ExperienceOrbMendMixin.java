package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code PlayerItemMendEvent#getDurabilityToXpOperation()} 的消费方。
 *
 * <p>Arclight 覆写的 {@code repairPlayerItems} 里那句 {@code int k = i - this.durabilityToXp(j);}
 * 就是 paper 换成 {@code event.getDurabilityToXpOperation().applyAsInt(j)} 的地方。
 *
 * <p>另补 paper 的递归保护：算子返回 0 且修复量也是 0 时，
 * {@code k} 恒等于 {@code i}，原代码会无限递归下去（栈溢出）。
 * {@code @Local} 的槽号取自 Arclight 编译产物 {@code ExperienceOrbMixin#repairPlayerItems}
 * 的 LocalVariableTable（slot 2 = {@code i}，slot 5 = {@code j}）。
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMendMixin {

    @WrapOperation(method = "repairPlayerItems",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;durabilityToXp(I)I"))
    private int paperarc$consumedExperience(ExperienceOrb orb, int repairAmount, Operation<Integer> original) {
        PlayerItemMendEvent event = EventCauseState.takeLastMendEvent();
        return event == null
                ? original.call(orb, repairAmount)
                : event.getDurabilityToXpOperation().applyAsInt(repairAmount);
    }

    @WrapOperation(method = "repairPlayerItems",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ExperienceOrb;repairPlayerItems(Lnet/minecraft/world/entity/player/Player;I)I"))
    private int paperarc$stopMendRecursion(ExperienceOrb orb, Player player, int remaining,
                                           Operation<Integer> original,
                                           @Local(argsOnly = true, index = 2) int incoming,
                                           @Local(index = 5) int repairAmount) {
        if (repairAmount == 0 && incoming == remaining) {
            return remaining;
        }
        return original.call(orb, player, remaining);
    }
}
