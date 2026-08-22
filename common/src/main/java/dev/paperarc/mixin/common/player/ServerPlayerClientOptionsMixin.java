package dev.paperarc.mixin.common.player;

import com.destroystokyo.paper.ClientOption;
import dev.paperarc.bridge.PaperArcBridge;
import dev.paperarc.util.PaperArcSkinParts;
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
 * 实现差异：vanilla 无 updateOptionsNoEvents 拆分。为复现"登录不发事件"，
 * 在构造器内 INVOKE updateOptions 处置 paperarc$login 标志；HEAD handler 见
 * 标志则跳过发事件并清标志。其余调用（玩家在设置界面改动后）正常发事件。
 * <p>
 * SkinParts 用 dev.paperarc.util.PaperArcSkinParts（Paper 的 PaperSkinParts 是
 * 服务端内部类不可用）；插件若 instanceof PaperSkinParts 会失败，见报告。
 * Arclight ServerPlayerMixin 同方法 HEAD 也注入（发 PlayerChangedMainHandEvent /
 * PlayerLocaleChangeEvent），均为非取消 Inject，与本 mixin 共存无冲突。
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerClientOptionsMixin {

    /** 构造器（登录）路径标志：跳过一次性事件发射。 */
    @Unique
    private boolean paperarc$login;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;updateOptions(Lnet/minecraft/server/level/ClientInformation;)V"
            )
    )
    private void paperarc$markLogin(CallbackInfo ci) {
        this.paperarc$login = true;
    }

    @Inject(method = "updateOptions", at = @At("HEAD"))
    private void paperarc$onUpdateOptions(ClientInformation clientOptions, CallbackInfo ci) {
        if (paperarc$login) {
            paperarc$login = false;
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
