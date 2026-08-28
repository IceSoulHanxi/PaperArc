package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Evoker;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEvoker;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftSheep;
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

    @Unique
    private static final String PAPERARC$WOLOLO_KEY = "paperarc.wololoTarget";

    @Shadow
    public abstract Evoker getHandle();

    @Unique
    @Nullable
    private Sheep paperarc$nmsWololoTarget() {
        Object custom = com.ixnah.mc.paperarc.bridge.ApiState.get(this, PAPERARC$WOLOLO_KEY, null);
        if (custom instanceof CraftSheep craftSheep) {
            Sheep sheep = craftSheep.getHandle();
            if (!sheep.isRemoved()) {
                return sheep;
            }
            com.ixnah.mc.paperarc.bridge.ApiState.remove(this, PAPERARC$WOLOLO_KEY);
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
            com.ixnah.mc.paperarc.bridge.ApiState.remove(this, PAPERARC$WOLOLO_KEY);
            getHandle().setTarget(null);
            return;
        }
        Sheep nms = ((CraftSheep) sheep).getHandle();
        com.ixnah.mc.paperarc.bridge.ApiState.put(this, PAPERARC$WOLOLO_KEY, sheep);
        getHandle().setTarget(nms);
    }
}
