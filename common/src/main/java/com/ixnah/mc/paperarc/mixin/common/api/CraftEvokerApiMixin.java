package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Evoker;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
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
 * explicit setter value lives in com.ixnah.mc.paperarc.bridge.ApiState and is pushed into
 * the vanilla mechanism the wololo goal uses (the evoker's attack target). The
 * getter prefers the side-map value and otherwise reports a Sheep attack target.
 */
@Mixin(CraftEvoker.class)
public abstract class CraftEvokerApiMixin {

    // Paper 在 NMS Evoker 上有 wololoTarget 字段；Arclight 无，注入 Craft 字段（CraftSheep 引用）。
    @Unique
    private org.bukkit.entity.Sheep wololoTarget;

    @Shadow
    public abstract Evoker getHandle();

    @Unique
    @Nullable
    private Sheep paperarc$nmsWololoTarget() {
        if (this.wololoTarget instanceof CraftSheep craftSheep) {
            Sheep sheep = craftSheep.getHandle();
            if (!sheep.isRemoved()) {
                return sheep;
            }
            this.wololoTarget = null;
        }
        return null;
    }

    @Unique
    @Nullable
    private org.bukkit.entity.Sheep paperarc$wrap(Sheep sheep) {
        if (sheep == null) {
            return null;
        }
        CraftServer server = (CraftServer) ((CraftEntity) (Object) this).getServer();
        return (org.bukkit.entity.Sheep) CraftEntity.getEntity(server, sheep);
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
            this.wololoTarget = null;
            getHandle().setTarget(null);
            return;
        }
        Sheep nms = ((CraftSheep) sheep).getHandle();
        this.wololoTarget = sheep;
        getHandle().setTarget(nms);
    }
}
