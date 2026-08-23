package dev.paperarc.mixin.common.api;

import net.minecraft.world.entity.monster.Slime;
import org.bukkit.craftbukkit.v.entity.CraftSlime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Slime wander API.
 *
 * Paper stores a {@code canWander} flag directly on NMS Slime (default true) and
 * uses it to gate SlimeRandomDirectionGoal#canUse. Arclight's spigot NMS has no
 * such field, so the flag lives in dev.paperarc.bridge.ApiState; note that the
 * vanilla pathfinder is not gated by this value without NMS-side support.
 */
@Mixin(CraftSlime.class)
public abstract class CraftSlimeApiMixin {

    @Unique
    private static final String PAPERARC$CAN_WANDER_KEY = "paperarc.canWander";

    @Shadow
    public abstract Slime getHandle();

    @Unique
    public boolean canWander() {
        return dev.paperarc.bridge.ApiState.get(this, PAPERARC$CAN_WANDER_KEY, Boolean.TRUE);
    }

    @Unique
    public void setWander(boolean canWander) {
        dev.paperarc.bridge.ApiState.put(this, PAPERARC$CAN_WANDER_KEY, canWander);
    }
}
