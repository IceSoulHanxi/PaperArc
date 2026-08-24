package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.boss.DragonBattle} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.boss.DragonBattle", remap = false)
public interface DragonBattleIfaceMixin {

    @Unique
    public abstract int getGatewayCount();

    @Unique
    public abstract boolean spawnNewGateway();

    @Unique
    public abstract void spawnNewGateway(io.papermc.paper.math.Position p0);

    @Unique
    public abstract java.util.List getRespawnCrystals();

    @Unique
    public abstract java.util.List getHealingCrystals();
}
