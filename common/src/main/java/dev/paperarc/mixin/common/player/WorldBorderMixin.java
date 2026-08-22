package dev.paperarc.mixin.common.player;

import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v.CraftWorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper "Add worldborder events"：WorldBorderCenterChangeEvent /
 * WorldBorderBoundsChangeEvent（INSTANT_MOVE 路径）。
 * <p>
 * 对照 Paper 补丁：setCenter/setSize/lerpSizeBetween 开头在 world != null 时
 * 发事件，取消则直接 return；非取消时用插件改写后的值继续原逻辑。
 * <p>
 * 实现差异：vanilla 1.21.1 WorldBorder 没有 Paper 的 world 字段（无法 @Shadow），
 * 改为遍历 CraftServer#getWorlds() 用 CraftWorldBorder#getHandle() 反查宿主世界
 * （CraftWorld 缓存其 worldBorder 实例，反查 O(世界数)，且这些方法调用频率极低）。
 * 找不到宿主世界时等价于 Paper 的 world == null 分支：不发事件，原逻辑照常。
 * <p>
 * 参数改写：@Inject 无法改参数，采用"取消 + 静默重入"模式：插件改写了值时置
 * paperarc$silent 后用新值重入原方法并 cancel 首次调用；重入时清标志直接放行，
 * 不重复发事件（等价于 Paper 用局部变量覆盖参数）。这些方法只在服务端主线程
 * 被调用，@Unique 标志无线程安全问题。
 * <p>
 * 注意：setSize 内插件改为 STARTED_MOVE 且 duration > 0 时调用 lerpSizeBetween，
 * 该方法自身也会发一次 BoundsChange 事件——与 Paper 上游行为完全一致。
 */
@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin {

    /** 静默重入标志：为 true 时跳过事件直接执行原逻辑。 */
    @Unique
    private boolean paperarc$silent;

    /** 反查 WorldBorder 宿主 Bukkit 世界；无宿主（未注册/虚拟）返回 null。 */
    static World paperarc$findWorld(WorldBorder handle) {
        for (World world : PaperArcBridge.getServer().getWorlds()) {
            if (((CraftWorldBorder) world.getWorldBorder()).getHandle() == handle) {
                return world;
            }
        }
        return null;
    }

    @Inject(method = "setCenter", at = @At("HEAD"), cancellable = true)
    private void paperarc$onSetCenter(double x, double z, CallbackInfo ci) {
        if (paperarc$silent) {
            paperarc$silent = false;
            return;
        }
        World bukkitWorld = paperarc$findWorld((WorldBorder) (Object) this);
        if (bukkitWorld == null) {
            return;
        }
        WorldBorderCenterChangeEvent event = new WorldBorderCenterChangeEvent(
                bukkitWorld,
                bukkitWorld.getWorldBorder(),
                new Location(bukkitWorld, ((WorldBorder) (Object) this).getCenterX(), 0, ((WorldBorder) (Object) this).getCenterZ()),
                new Location(bukkitWorld, x, 0, z));
        if (!event.callEvent()) {
            ci.cancel();
            return;
        }
        double newX = event.getNewCenter().getX();
        double newZ = event.getNewCenter().getZ();
        if (newX != x || newZ != z) {
            paperarc$silent = true;
            ((WorldBorder) (Object) this).setCenter(newX, newZ);
            ci.cancel();
        }
    }

    @Inject(method = "setSize", at = @At("HEAD"), cancellable = true)
    private void paperarc$onSetSize(double size, CallbackInfo ci) {
        if (paperarc$silent) {
            paperarc$silent = false;
            return;
        }
        World bukkitWorld = paperarc$findWorld((WorldBorder) (Object) this);
        if (bukkitWorld == null) {
            return;
        }
        WorldBorderBoundsChangeEvent event = new WorldBorderBoundsChangeEvent(
                bukkitWorld,
                bukkitWorld.getWorldBorder(),
                WorldBorderBoundsChangeEvent.Type.INSTANT_MOVE,
                ((WorldBorder) (Object) this).getSize(),
                size,
                0L);
        if (!event.callEvent()) {
            ci.cancel();
            return;
        }
        if (event.getType() == WorldBorderBoundsChangeEvent.Type.STARTED_MOVE && event.getDuration() > 0L) {
            // 插件把即时调整改为定时过渡：走 lerpSizeBetween（会再发一次事件，与 Paper 一致）
            ((WorldBorder) (Object) this).lerpSizeBetween(event.getOldSize(), event.getNewSize(), event.getDuration());
            ci.cancel();
            return;
        }
        double newSize = event.getNewSize();
        if (newSize != size) {
            paperarc$silent = true;
            ((WorldBorder) (Object) this).setSize(newSize);
            ci.cancel();
        }
    }

    @Inject(method = "lerpSizeBetween", at = @At("HEAD"), cancellable = true)
    private void paperarc$onLerpSizeBetween(double fromSize, double toSize, long time, CallbackInfo ci) {
        if (paperarc$silent) {
            paperarc$silent = false;
            return;
        }
        World bukkitWorld = paperarc$findWorld((WorldBorder) (Object) this);
        if (bukkitWorld == null) {
            return;
        }
        WorldBorderBoundsChangeEvent.Type type = fromSize == toSize
                ? WorldBorderBoundsChangeEvent.Type.INSTANT_MOVE
                : WorldBorderBoundsChangeEvent.Type.STARTED_MOVE;
        WorldBorderBoundsChangeEvent event = new WorldBorderBoundsChangeEvent(
                bukkitWorld,
                bukkitWorld.getWorldBorder(),
                type,
                fromSize,
                toSize,
                time);
        if (!event.callEvent()) {
            ci.cancel();
            return;
        }
        double newTo = event.getNewSize();
        long newTime = event.getDuration();
        if (newTo != toSize || newTime != time) {
            paperarc$silent = true;
            ((WorldBorder) (Object) this).lerpSizeBetween(fromSize, newTo, newTime);
            ci.cancel();
        }
    }
}
