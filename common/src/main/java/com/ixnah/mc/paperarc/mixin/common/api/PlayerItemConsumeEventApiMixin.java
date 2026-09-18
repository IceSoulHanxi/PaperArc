package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code PlayerItemConsumeEvent#getReplacement()/setReplacement(ItemStack)}
 * （Custom-replacement-for-eaten-items.patch）。
 *
 * <p>消费方见 {@code entity.LivingEntityItemConsumeReplacementMixin}：吃完之后放回手里的
 * 那个物品被换成插件给的这一个。运行时的两个构造器里三参那个是终端（二参委托给它）。
 */
@Mixin(PlayerItemConsumeEvent.class)
public abstract class PlayerItemConsumeEventApiMixin {

    @Unique
    private ItemStack paperarc$replacement;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/inventory/ItemStack;Lorg/bukkit/inventory/EquipmentSlot;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$register(Player player, ItemStack item, EquipmentSlot hand, CallbackInfo ci) {
        this.paperarc$replacement = null;
        PaperarcEventCauses.rememberItemConsumeEvent((PlayerItemConsumeEvent) (Object) this);
    }

    @Unique
    public ItemStack getReplacement() {
        return this.paperarc$replacement;
    }

    @Unique
    public void setReplacement(ItemStack replacement) {
        this.paperarc$replacement = replacement;
    }
}
