package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code FurnaceBurnEvent#willConsumeFuel()/setConsumeFuel(boolean)}。
 *
 * <p>消费方在 {@code AbstractFurnaceBlockEntity#serverTick}：paper 把点火那一支的
 * "扣一个燃料"守起来，效果就是"烧了但不耗燃料"——见 {@code block.AbstractFurnaceConsumeFuelMixin}。
 * 事件对象靠构造器登记（Arclight 的构造点在它自己的 handler 里，选不了）。
 */
@Mixin(FurnaceBurnEvent.class)
public abstract class FurnaceBurnEventApiMixin {

    @Unique
    private boolean paperarc$consumeFuel = true;

    @Inject(method = "<init>(Lorg/bukkit/block/Block;Lorg/bukkit/inventory/ItemStack;I)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$register(Block furnace, ItemStack fuel, int burnTime, CallbackInfo ci) {
        this.paperarc$consumeFuel = true;
        PaperarcEventCauses.rememberFurnaceBurnEvent((FurnaceBurnEvent) (Object) this);
    }

    @Unique
    public boolean willConsumeFuel() {
        return this.paperarc$consumeFuel;
    }

    @Unique
    public void setConsumeFuel(boolean consumeFuel) {
        this.paperarc$consumeFuel = consumeFuel;
    }
}
