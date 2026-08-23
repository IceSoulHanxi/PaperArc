package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.boss.DragonBattle} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.boss.DragonBattle", remap = false)
public interface DragonBattleIfaceMixin {

    public abstract int getGatewayCount();

    public abstract boolean spawnNewGateway();

    public abstract void spawnNewGateway(io.papermc.paper.math.Position p0);

    public abstract java.util.List getRespawnCrystals();

    public abstract java.util.List getHealingCrystals();
}
