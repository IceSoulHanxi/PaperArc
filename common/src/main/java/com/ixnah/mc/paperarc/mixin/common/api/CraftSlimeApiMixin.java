package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.SlimeBridge;
import net.minecraft.world.entity.monster.Slime;
import org.bukkit.craftbukkit.v.entity.CraftSlime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Slime wander API.
 *
 * <p>Paper stores a {@code canWander} flag directly on NMS Slime (default
 * true) and uses it to gate SlimeRandomDirectionGoal#canUse. The field is
 * injected into the NMS class by {@code SlimeFieldsMixin} and reached here
 * through {@link SlimeBridge}. Paper adds NMS accessor methods
 * {@code canWander()} / {@code setWander(boolean)}, exposed under those names.
 */
@Mixin(CraftSlime.class)
public abstract class CraftSlimeApiMixin {

    @Shadow
    public abstract Slime getHandle();

    @Unique
    public boolean canWander() {
        return ((SlimeBridge) this.getHandle()).canWander();
    }

    @Unique
    public void setWander(boolean canWander) {
        ((SlimeBridge) this.getHandle()).setWander(canWander);
    }
}
