package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.ixnah.mc.paperarc.bridge.PaperarcAdventure;
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
 * {@code InventoryOpenEvent} 触发之后；与其在三个宿主上各挂一个 {@code @ModifyArg}，
 * 不如直接在包的构造器上收口 —— 标题最终都要经过这里。
 *
 * <p>只在事件真的设了 override 时才改；取即清，所以一次 override 只作用于
 * 事件之后构造的第一个开界面包。马的背包走
 * {@code ClientboundHorseScreenOpenPacket}（没有标题字段），与 paper 一样不受影响。
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
        InventoryOpenEvent event = EventCauseState.takeLastInventoryOpenEvent();
        if (event == null || event.titleOverride() == null) {
            return;
        }
        Component override = PaperarcAdventure.asVanilla(event.titleOverride());
        if (override != null) {
            this.title = override;
        }
    }
}
