package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code PlayerItemConsumeEvent#getReplacement()} 的消费方。
 *
 * <p>paper 把 {@code finishUsingItem} 的返回值换成插件给的替代品，再 {@code setItemInHand}。
 * Arclight 把那次 {@code finishUsingItem} 用 {@code @Eject} 接管了（handler 方法名带随机段，
 * 选不了），但它外面那层 Forge 的
 * {@code ForgeEventFactory.onItemUseFinish(...)} 原样保留（`javap -c` 核对
 * 官方名映射后的 {@code LivingEntity#completeUsingItem}：offset 97），
 * 它的返回值就是最终要放回手里的那一份 —— 锚这里等价且更稳。
 *
 * <p>替代品是新建对象，必然 {@code != this.useItem}，所以后面那句
 * {@code setItemInHand} 一定会执行；同 paper，替换生效后刷一次客户端背包。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityItemConsumeReplacementMixin {

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void paperarc$resetConsumeEvent(CallbackInfo ci) {
        EventCauseState.clearLastConsumeEvent();
    }

    @ModifyExpressionValue(method = "completeUsingItem",
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraftforge/event/ForgeEventFactory;onItemUseFinish(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack paperarc$applyConsumeReplacement(ItemStack result) {
        PlayerItemConsumeEvent event = EventCauseState.takeLastConsumeEvent();
        if (event == null || event.getReplacement() == null) {
            return result;
        }
        if ((Object) this instanceof ServerPlayer player) {
            PaperArcBridge.<org.bukkit.entity.Player>bukkitEntity(player).updateInventory();
        }
        return CraftItemStack.asNMSCopy(event.getReplacement());
    }
}
