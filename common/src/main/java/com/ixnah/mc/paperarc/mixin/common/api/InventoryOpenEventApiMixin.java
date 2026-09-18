package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
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
 * <p>消费方是发给客户端的那个 {@code ClientboundOpenScreenPacket} 的标题：
 * {@code ServerPlayerOpenMenuTitleMixin}（方块容器一路）与
 * {@code CraftHumanEntityOpenTitleMixin}（插件 {@code openInventory} 一路）。
 * 与 paper 一样，它**不**改 {@code InventoryView#title()}，只改这一个包 ——
 * 马的背包是例外（走 {@code ClientboundHorseScreenOpenPacket}，没有标题字段）。
 */
@Mixin(InventoryOpenEvent.class)
public abstract class InventoryOpenEventApiMixin {

    @Unique
    private Component paperarc$titleOverride;

    @Inject(method = "<init>(Lorg/bukkit/inventory/InventoryView;)V", at = @At("RETURN"), remap = false)
    private void paperarc$register(InventoryView transaction, CallbackInfo ci) {
        this.paperarc$titleOverride = null;
        EventCauseState.setLastInventoryOpenEvent((InventoryOpenEvent) (Object) this);
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
