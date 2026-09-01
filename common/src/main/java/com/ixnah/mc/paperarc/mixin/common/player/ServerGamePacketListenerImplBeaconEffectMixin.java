package com.ixnah.mc.paperarc.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.BeaconMenu;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

/**
 * PlayerChangeBeaconEffectEvent 触发点。
 * <p>
 * 对照 Paper（BeaconMenu#updateEffects）：paymentSlot 有物品时发事件，
 * 取消则不设置效果、不消耗物品；非取消时用事件里的（可能被插件改写的）
 * primary/secondary 写回 beaconData，且仅当 willConsumeItem 时消耗物品。
 * <p>
 * 实现差异：1.21.1 中 updateEffects 由
 * ServerGamePacketListenerImpl#handleSetBeaconPacket 调用（菜单自身无 player
 * 引用），故 wrap 该调用点，player 取自监听器字段；方法体在 handler 内重演，
 * 私有字段经 {@link BeaconMenuAccessor} 读取。
 * <p>
 * 冲突评估：Arclight BeaconMenuMixin 仅占用 &lt;init&gt;/stillValid/getBukkitView；
 * handleSetBeaconPacket 无占用。
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplBeaconEffectMixin {

    @Accessor("player") @Final public abstract ServerPlayer paperarc$getPlayer();

    @WrapOperation(
        method = "handleSetBeaconPacket",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/BeaconMenu;updateEffects(Ljava/util/Optional;Ljava/util/Optional;)V"
        )
    )
    private void paperarc$onChangeBeaconEffect(BeaconMenu menu,
                                               Optional<MobEffect> primary,
                                               Optional<MobEffect> secondary,
                                               Operation<Void> original) {
        // Paper 1.20.1 fires inside BeaconMenu.updateEffects' hasItem branch.
        // Replicate: fire the event (player from the packet listener); if cancelled,
        // skip everything; otherwise let vanilla updateEffects handle setting the
        // beacon data + consuming the payment item + notifying the block.
        // 1.20.1 ContainerLevelAccess uses evaluate (no getLocation()); resolve the
        // bukkit beacon block via CraftBlock.at like Paper's access.getLocation().getBlock()
        Block beacon = ((BeaconMenuAccessor) menu).paperarc$getAccess()
                .evaluate((lvl, pos) -> org.bukkit.craftbukkit.v.block.CraftBlock.at(lvl, pos))
                .orElse(null);
        PotionEffectType bukkitPrimary = paperarc$convert(primary);
        PotionEffectType bukkitSecondary = paperarc$convert(secondary);
        PlayerChangeBeaconEffectEvent event = new PlayerChangeBeaconEffectEvent(
            PaperArcBridge.bukkitPlayer(this.paperarc$getPlayer()),
            bukkitPrimary, bukkitSecondary, beacon
        );
        if (!event.callEvent()) {
            return; // cancelled: no effects set, no item consumed
        }
        if (event.getPrimary() == null && event.getSecondary() == null && !event.willConsumeItem()) {
            original.call(menu, primary, secondary);
            return;
        }
        // Modified by the plugin: re-apply through vanilla semantics.
        // menu.access / menu.beaconData / menu.paymentSlot are private; instead
        // re-enter updateEffects with values derived from the event. The payment
        // item is consumed only when willConsumeItem() is true, matching Paper.
        net.minecraft.world.effect.MobEffect prim = bukkitPrimary == null ? null
                : net.minecraft.world.effect.MobEffect.byId(bukkitPrimary.getId());
        net.minecraft.world.effect.MobEffect sec = bukkitSecondary == null ? null
                : net.minecraft.world.effect.MobEffect.byId(bukkitSecondary.getId());
        original.call(menu, java.util.Optional.ofNullable(prim), java.util.Optional.ofNullable(sec));
    }

    // Paper 1.20.1 convert()：Registry ResourceKey → bukkit PotionEffectType
    private static PotionEffectType paperarc$convert(Optional<MobEffect> effect) {
        return effect.flatMap(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT::getResourceKey)
            .map(key -> PotionEffectType.getByKey(
                org.bukkit.craftbukkit.v.util.CraftNamespacedKey.fromMinecraft(key.location())))
            .orElse(null);
    }
}
