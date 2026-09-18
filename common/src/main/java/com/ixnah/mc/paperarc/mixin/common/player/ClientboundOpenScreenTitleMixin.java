package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.api.PaperarcComponents;
import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code InventoryOpenEvent#titleOverride()} 的消费方。
 *
 * <p>发这个包的地方有三处（{@code ServerPlayer#openMenu} 与 CraftBukkit 的
 * {@code CraftHumanEntity#openInventory/openCustomInventory}），且都在
 * {@code InventoryOpenEvent} 之后；与其在三个宿主上各挂一个 {@code @ModifyArg}，
 * 不如直接在包的构造器上收口 —— 标题最终都要经过这里。
 *
 * <p>取即清，所以一次 override 只作用于事件之后构造的第一个开界面包。
 */
@Mixin(ClientboundOpenScreenPacket.class)
public abstract class ClientboundOpenScreenTitleMixin {

    @Shadow
    @Final
    @Mutable
    private Component title;

    @Inject(method = "<init>(ILnet/minecraft/world/inventory/MenuType;Lnet/minecraft/network/chat/Component;)V",
            at = @At("RETURN"))
    private void paperarc$applyTitleOverride(int containerId, MenuType<?> type, Component title, CallbackInfo ci) {
        InventoryOpenEvent event = PaperarcEventCauses.takeInventoryOpenEvent();
        if (event == null || event.titleOverride() == null) {
            return;
        }
        Component override = PaperarcComponents.toVanilla(event.titleOverride());
        if (override != null) {
            this.title = override;
        }
    }
}
