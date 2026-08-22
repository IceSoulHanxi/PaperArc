package dev.paperarc.mixin.common;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.event.PaperArcEvents;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * PlayerJumpEvent 触发点。
 * <p>
 * 对照 Paper：在 handleMovePlayer 的 jumpFromGround() 调用前发事件，
 * 取消时不执行跳跃并回传玩家位置。
 * <p>
 * 冲突评估（docs/conflict-matrix.md #2）：handleMovePlayer 被 Arclight
 * @Overwrite 接管，但 jumpFromGround 调用指令保留在其覆写体中；
 * 本 mixin 只 wrap 该指令本身，不与其 Overwrite 竞争方法体。
 *
 * <p>v1 已知简化（待真实代码核对后改进）：
 * <ul>
 *   <li>Bukkit Player 通过 UUID 从注册表取，而非 ServerPlayer#getBukkitEntity()
 *       （后者需要 CraftBukkit 类参与编译，等 arclight_repo 工件就绪后切换）</li>
 *   <li>from/to 未使用移动包数据与 lastPos 字段，均为当前坐标</li>
 *   <li>取消时用 absMoveTo 回传，未走 internalTeleport 完整路径</li>
 * </ul>
 */
@Mixin(net.minecraft.server.network.ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @WrapOperation(
        method = "handleMovePlayer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;jumpFromGround()V")
    )
    private void paperarc$onPlayerJump(ServerPlayer instance, Operation<Void> original) {
        Player bukkit = Bukkit.getPlayer(instance.getUUID());
        if (bukkit == null) {
            original.call(instance);
            return;
        }
        Location from = bukkit.getLocation().clone();
        Location to = from.clone(); // TODO(v2): 按 Paper 用移动包 pos/rot 填充
        PlayerJumpEvent event = new PlayerJumpEvent(bukkit, from, to);
        PaperArcEvents.fire(event);
        if (!event.isCancelled()) {
            original.call(instance);
        } else {
            // TODO(v2): Paper 取消时经 internalTeleport 回传，含相对集合语义
            instance.absMoveTo(from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch());
        }
    }
}
