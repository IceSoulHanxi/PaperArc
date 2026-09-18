package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Evoker;
import org.bukkit.craftbukkit.v.entity.CraftEvoker;
import org.bukkit.craftbukkit.v.entity.CraftSheep;
import org.jetbrains.annotations.Nullable;
import com.ixnah.mc.paperarc.bridge.EvokerWololoBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Evoker wololo-target API.
 *
 * Paper stores the wololo target on NMS Evoker (getWololoTarget/setWololoTarget,
 * publicized by an AT). Arclight's spigot NMS has neither method nor field, so the
 * explicit setter value lives in com.ixnah.mc.paperarc.bridge.ApiState and is pushed into
 * the vanilla mechanism the wololo goal uses (the evoker's attack target). The
 * getter prefers the side-map value and otherwise reports a Sheep attack target.
 */
@Mixin(CraftEvoker.class)
public abstract class CraftEvokerApiMixin {

    @Shadow
    public abstract Evoker getHandle();

    @Unique
    @Nullable
    private Sheep paperarc$nmsWololoTarget() {
        // A8/Y-3：字段挪到 NMS Evoker 上（EvokerFieldsMixin），与 Paper 同宿主
        Sheep sheep = ((EvokerWololoBridge) getHandle()).paper$getWololoTarget();
        if (sheep != null) {
            if (!sheep.isRemoved()) {
                return sheep;
            }
            ((EvokerWololoBridge) getHandle()).paper$setWololoTarget(null);
        }
        return null;
    }

    @Unique
    @Nullable
    private org.bukkit.entity.Sheep paperarc$wrap(Sheep sheep) {
        return sheep == null ? null : PaperArcBridge.<org.bukkit.entity.Sheep>bukkitEntity(sheep);
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
        if (sheep == null) {
            ((EvokerWololoBridge) getHandle()).paper$setWololoTarget(null);
            getHandle().setTarget(null);
            return;
        }
        Sheep nms = ((CraftSheep) sheep).getHandle();
        ((EvokerWololoBridge) getHandle()).paper$setWololoTarget(nms);
        getHandle().setTarget(nms);
    }
}
