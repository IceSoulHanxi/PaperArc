package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Item} (generated).
 * Adds 8 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Item", remap = false)
public interface ItemIfaceMixin extends io.papermc.paper.entity.Frictional {

    @Unique
    public abstract boolean canMobPickup();

    @Unique
    public abstract void setCanMobPickup(boolean p0);

    @Unique
    public abstract boolean canPlayerPickup();

    @Unique
    public abstract void setCanPlayerPickup(boolean p0);

    @Unique
    public abstract boolean willAge();

    @Unique
    public abstract void setWillAge(boolean p0);

    @Unique
    public abstract int getHealth();

    @Unique
    public abstract void setHealth(int p0);

    /**
     * paper {@code Frictional}（B3-2）：NOT_SET 时保持 vanilla 行为。
     * 状态存 NMS 注入字段，生效点见 {@code entity.LivingEntityFrictionMixin} /
     * {@code entity.ItemEntityFrictionMixin}。
     */
    @Unique
    public default net.kyori.adventure.util.TriState getFrictionState() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.getFrictionState(this);
    }

    @Unique
    public default void setFrictionState(net.kyori.adventure.util.TriState state) {
        com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.setFrictionState(this, state);
    }
}
