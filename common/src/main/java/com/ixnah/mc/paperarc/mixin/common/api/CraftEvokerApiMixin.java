package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Evoker;
import org.bukkit.craftbukkit.v.entity.CraftEvoker;
import org.bukkit.craftbukkit.v.entity.CraftSheep;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Evoker wololo-target API.
 *
 * Paper stores the wololo target on NMS Evoker (getWololoTarget/setWololoTarget,
 * publicized by an AT). Arclight's spigot NMS has neither method nor field, so the
 * B8/Y-3：不再自己存一份 —— vanilla {@code Evoker} 本来就有 {@code wololoTarget}
 * 字段与包私有的 {@code get/setWololoTarget}（`javap -p` 核对，Paper 只是把它们 publicize），
 * 由 {@code entity.EvokerFieldsMixin} 转发过去，所以设进去的目标就是施法逻辑真正在用的那个。
 * 没显式设过时回落到"当前攻击目标恰好是绵羊"，与 vanilla 的观感一致。
 */
@Mixin(CraftEvoker.class)
public abstract class CraftEvokerApiMixin {

    @Shadow
    public abstract Evoker getHandle();

    @Unique
    @Nullable
    private Sheep paperarc$nmsWololoTarget() {
        com.ixnah.mc.paperarc.bridge.EvokerWololoBridge bridge =
                (com.ixnah.mc.paperarc.bridge.EvokerWololoBridge) getHandle();
        Sheep custom = bridge.paper$getWololoTarget();
        if (custom != null) {
            if (!custom.isRemoved()) {
                return custom;
            }
            bridge.paper$setWololoTarget(null);
        }
        return null;
    }

    @Unique
    @Nullable
    private org.bukkit.entity.Sheep paperarc$wrap(Sheep sheep) {
        if (sheep == null) {
            return null;
        }
        return com.ixnah.mc.paperarc.bridge.PaperArcBridge.bukkitEntity(sheep);
    }

    @Unique
    @Nullable
    public org.bukkit.entity.Sheep getWololoTarget() {
        Sheep explicit = paperarc$nmsWololoTarget();
        if (explicit != null) {
            return paperarc$wrap(explicit);
        }
        // Vanilla wololo operates on the evoker's current target while casting.
        return paperarc$wrap(getHandle().getTarget() instanceof Sheep sheep ? sheep : null);
    }

    @Unique
    public void setWololoTarget(@Nullable org.bukkit.entity.Sheep sheep) {
        com.ixnah.mc.paperarc.bridge.EvokerWololoBridge bridge =
                (com.ixnah.mc.paperarc.bridge.EvokerWololoBridge) getHandle();
        if (sheep == null) {
            bridge.paper$setWololoTarget(null);
            return;
        }
        Sheep nms = ((CraftSheep) sheep).getHandle();
        bridge.paper$setWololoTarget(nms);
    }
}
