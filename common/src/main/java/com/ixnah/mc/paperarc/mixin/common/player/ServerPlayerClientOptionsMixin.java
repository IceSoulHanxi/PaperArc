package com.ixnah.mc.paperarc.mixin.common.player;

import com.destroystokyo.paper.ClientOption;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.ixnah.mc.paperarc.util.PaperArcSkinParts;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import org.bukkit.inventory.MainHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * PlayerClientOptionsChangeEvent 触发点。
 * <p>
 * 对照 Paper 补丁：ServerPlayer#updateOptions 开头发事件（携带全部客户端选项
 * 构造的 Map），登录路径改走 updateOptionsNoEvents 不发事件。
 * <p>
 * 实现差异：vanilla 无 updateOptionsNoEvents 拆分。登录路径判定不依赖构造器
 * 注入（Forge 的 sponge mixin 禁止以构造器为注入目标，fabric fork 允许）：
 * vanilla 构造器调用 updateOptions 时 {@code connection} 尚未赋值，而玩家在
 * 设置界面改动时连接必然已建立——据此区分两条路径。
 * <p>
 * SkinParts 用 com.ixnah.mc.paperarc.util.PaperArcSkinParts（Paper 的 PaperSkinParts 是
 * 服务端内部类不可用）；插件若 instanceof PaperSkinParts 会失败，见报告。
 * Arclight ServerPlayerMixin 同方法 HEAD 也注入（发 PlayerChangedMainHandEvent /
 * PlayerLocaleChangeEvent），均为非取消 Inject，与本 mixin 共存无冲突。
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerClientOptionsMixin {

    /** 构造器（登录）路径：connection 尚未建立。 */
    @Unique
    private boolean paperarc$isLoginPath() {
        return ((ServerPlayer) (Object) this).connection == null;
    }

    @Inject(method = "updateOptions", at = @At("HEAD"))
    private void paperarc$onUpdateOptions(ClientInformation clientOptions, CallbackInfo ci) {
        if (paperarc$isLoginPath()) {
            return;
        }
        Map<ClientOption<?>, Object> newValues = new HashMap<>();
        newValues.put(ClientOption.LOCALE, clientOptions.language());
        newValues.put(ClientOption.VIEW_DISTANCE, clientOptions.viewDistance());
        newValues.put(ClientOption.CHAT_VISIBILITY,
                ClientOption.ChatVisibility.valueOf(clientOptions.chatVisibility().name()));
        newValues.put(ClientOption.CHAT_COLORS_ENABLED, clientOptions.chatColors());
        newValues.put(ClientOption.SKIN_PARTS, new PaperArcSkinParts(clientOptions.modelCustomisation()));
        newValues.put(ClientOption.MAIN_HAND,
                clientOptions.mainHand() == HumanoidArm.LEFT ? MainHand.LEFT : MainHand.RIGHT);
        newValues.put(ClientOption.ALLOW_SERVER_LISTINGS, clientOptions.allowsListing());
        newValues.put(ClientOption.TEXT_FILTERING_ENABLED, clientOptions.textFilteringEnabled());
        new com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent(
                PaperArcBridge.bukkitPlayer((ServerPlayer) (Object) this), newValues).callEvent();
    }
}
