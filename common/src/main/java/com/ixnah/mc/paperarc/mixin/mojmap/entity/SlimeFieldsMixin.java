package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.SlimeBridge;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code Slime.canWander} supplementary field
 * (Slime-Pathfinder-Events.patch). Field name matches Paper exactly (no
 * {@code paperarc$} prefix) so reflection on the NMS class is ABI-compatible
 * with Paper. Paper also adds NMS accessor methods {@code canWander()} and
 * {@code setWander(boolean)}, which the bridge exposes under those names.
 */
@Mixin(Slime.class)
public abstract class SlimeFieldsMixin implements SlimeBridge {

    @Unique
    public boolean canWander = true; // Paper

    @Override
    public boolean canWander() {
        return this.canWander;
    }

    @Override
    public void setWander(boolean canWander) {
        this.canWander = canWander;
    }
}
