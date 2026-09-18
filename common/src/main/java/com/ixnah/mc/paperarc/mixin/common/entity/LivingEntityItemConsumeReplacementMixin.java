package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
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
 * 这里锚 {@code LivingEntity#completeUsingItem} 里那唯一一次
 * {@code ItemStack#finishUsingItem(Level, LivingEntity)}，取其返回值替换 —— 与 paper 同一处。
 *
 * <p>替代品是新建对象，必然 {@code != this.useItem}，所以后面那句 {@code setItemInHand}
 * 一定会执行；同 paper，替换生效后刷一次客户端背包。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityItemConsumeReplacementMixin {

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void paperarc$resetConsumeEvent(CallbackInfo ci) {
        PaperarcEventCauses.popItemConsumeEvent();
    }

    @ModifyExpressionValue(method = "completeUsingItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack paperarc$applyConsumeReplacement(ItemStack result) {
        PlayerItemConsumeEvent event = PaperarcEventCauses.takeItemConsumeEvent();
        if (event == null || event.getReplacement() == null) {
            return result;
        }
        if ((Object) this instanceof ServerPlayer player) {
            PaperArcBridge.<org.bukkit.entity.Player>bukkitEntity(player).updateInventory();
        }
        return CraftItemStack.asNMSCopy(event.getReplacement());
    }
}
