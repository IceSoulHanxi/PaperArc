package dev.paperarc.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import net.minecraft.core.Holder;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Shadow @Final public ServerPlayer player;

    @WrapOperation(
        method = "handleSetBeaconPacket",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/BeaconMenu;updateEffects(Ljava/util/Optional;Ljava/util/Optional;)V"
        )
    )
    private void paperarc$onChangeBeaconEffect(BeaconMenu menu,
                                               Optional<Holder<MobEffect>> primary,
                                               Optional<Holder<MobEffect>> secondary,
                                               Operation<Void> original) {
        BeaconMenuAccessor accessor = (BeaconMenuAccessor) menu;
        if (!menu.getSlot(0).hasItem()) { // slot 0 = paymentSlot（BeaconMenu 构造时首个加入）
            original.call(menu, primary, secondary); // 原体同样什么都不做
            return;
        }
        PotionEffectType bukkitPrimary = primary.map(CraftPotionEffectType::minecraftHolderToBukkit).orElse(null);
        PotionEffectType bukkitSecondary = secondary.map(CraftPotionEffectType::minecraftHolderToBukkit).orElse(null);
        Block beacon = accessor.paperarc$getAccess().evaluate(CraftBlock::at).orElse(null);
        PlayerChangeBeaconEffectEvent event = new PlayerChangeBeaconEffectEvent(
            PaperArcBridge.bukkitPlayer(this.player),
            bukkitPrimary, bukkitSecondary, beacon
        );
        if (!event.callEvent()) {
            return; // 取消：不设置效果、不消耗物品
        }
        ContainerData beaconData = accessor.paperarc$getBeaconData();
        beaconData.set(1, event.getPrimary() == null ? BeaconMenu.encodeEffect(null)
            : BeaconMenu.encodeEffect(CraftPotionEffectType.bukkitToMinecraftHolder(event.getPrimary())));
        beaconData.set(2, event.getSecondary() == null ? BeaconMenu.encodeEffect(null)
            : BeaconMenu.encodeEffect(CraftPotionEffectType.bukkitToMinecraftHolder(event.getSecondary())));
        if (event.willConsumeItem()) {
            menu.getSlot(0).remove(1);
        }
        accessor.paperarc$getAccess().execute(Level::blockEntityChanged);
    }
}
