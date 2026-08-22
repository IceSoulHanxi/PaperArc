package dev.paperarc.mixin.common.player;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * PlayerElytraBoostEvent 触发点。
 * <p>
 * 对照 Paper 补丁：FireworkRocketItem#use 鞘翅飞行喷射火箭分支——发事件
 * （mirror ItemStack、待生成 Firework、hand），取消则不生成火箭、不消耗、
 * 不发统计，仅刷新服务端背包；非取消且 addFreshEntity 成功后发统计，
 * shouldConsume() 且非创造（hasInfiniteMaterials）才 shrink(1)，否则
 * （或 addFreshEntity 失败）刷新服务端背包。语义与 Paper 1.21.1 一致。
 * <p>
 * 实现：WrapOperation use 内 Level#addFreshEntity INVOKE（use 中唯一一次调用；
 * useOn 的方块放置路径不受影响）。事件 ItemStack 用 user.getItemInHand(hand)
 * 的 mirror——与补丁中的局部 itemStack 为同一实例。Arclight 只 mixin 了
 * FireworkRocketEntity（实体侧），未占用 FireworkRocketItem#use，无冲突。
 */
@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemElytraBoostMixin {

    @WrapOperation(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean paperarc$onElytraBoost(Level world, Entity entity, Operation<Boolean> original,
                                           Level methodWorld, Player user, InteractionHand hand) {
        var fireworkRocketEntity = (net.minecraft.world.entity.projectile.FireworkRocketEntity) entity;
        ItemStack itemStack = user.getItemInHand(hand);
        PlayerElytraBoostEvent event = new PlayerElytraBoostEvent(
                PaperArcBridge.bukkitPlayer(user),
                CraftItemStack.asCraftMirror(itemStack),
                (org.bukkit.entity.Firework) PaperArcBridge.bukkitEntity(fireworkRocketEntity),
                CraftEquipmentSlot.getHand(hand));
        if (!event.callEvent()) {
            if (user instanceof ServerPlayer serverPlayer) {
                PaperArcBridge.bukkitPlayer(serverPlayer).updateInventory();
            }
            return false;
        }
        boolean added = original.call(world, entity);
        if (added) {
            user.awardStat(Stats.ITEM_USED.get((FireworkRocketItem) (Object) this));
            if (event.shouldConsume() && !user.hasInfiniteMaterials()) {
                itemStack.shrink(1);
            } else if (user instanceof ServerPlayer serverPlayer) {
                PaperArcBridge.bukkitPlayer(serverPlayer).updateInventory();
            }
        } else if (user instanceof ServerPlayer serverPlayer) {
            PaperArcBridge.bukkitPlayer(serverPlayer).updateInventory();
        }
        return added;
    }
}
