package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code InventoryOpenEvent#titleOverride()/titleOverride(Component)}
 * （Add-titleOverride-to-InventoryOpenEvent.patch）。
 *
 * <p>消费方是发给客户端的那个 {@code ClientboundOpenScreenPacket} 的标题
 * （{@code player.ClientboundOpenScreenTitleMixin}）。与 paper 一样，它**不**改
 * {@code InventoryView#title()}；马的背包走 {@code ClientboundHorseScreenOpenPacket}
 * （没有标题字段），同样不受影响。
 */
@Mixin(InventoryOpenEvent.class)
public abstract class InventoryOpenEventApiMixin {

    @Unique
    private Component paperarc$titleOverride;

    @Inject(method = "<init>(Lorg/bukkit/inventory/InventoryView;)V", at = @At("RETURN"), remap = false)
    private void paperarc$register(InventoryView transaction, CallbackInfo ci) {
        this.paperarc$titleOverride = null;
        PaperarcEventCauses.rememberInventoryOpenEvent((InventoryOpenEvent) (Object) this);
    }

    @Unique
    public Component titleOverride() {
        return this.paperarc$titleOverride;
    }

    @Unique
    public void titleOverride(Component titleOverride) {
        this.paperarc$titleOverride = titleOverride;
    }
}
