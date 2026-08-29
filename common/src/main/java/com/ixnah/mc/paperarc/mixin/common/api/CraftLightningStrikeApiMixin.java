package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.LightningBolt;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLightningStrike;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's More-lightning-API additions on
 * {@link CraftLightningStrike}: {@code getLifeTicks()},
 * {@code setLifeTicks(int)}, {@code getFlashCount()},
 * {@code setFlashCount(int)}, {@code getCausingEntity()} and
 * {@code setCausingPlayer(Player)}.
 *
 * Vanilla NMS keeps the {@code life}/{@code flashes} counters private (Paper
 * widens them via AT); the runtime jar is srg-mapped, so string reflection
 * against the official names can never resolve. The {@code @Accessor} methods
 * on {@link LightningBoltAccessorMixin} are remapped by the refmap and merged
 * into the NMS class directly. {@code getCause()}/{@code setCause()} are
 * public vanilla.
 */
@Mixin(CraftLightningStrike.class)
public abstract class CraftLightningStrikeApiMixin {

    @Shadow
    public abstract LightningBolt getHandle();

    @Unique
    public int getLifeTicks() {
        return ((LightningBoltAccessorMixin) this.getHandle()).paperarc$getLife();
    }

    @Unique
    public void setLifeTicks(int lifeTicks) {
        ((LightningBoltAccessorMixin) this.getHandle()).paperarc$setLife(lifeTicks);
    }

    @Unique
    public int getFlashCount() {
        return ((LightningBoltAccessorMixin) this.getHandle()).paperarc$getFlashes();
    }

    @Unique
    public void setFlashCount(int flashes) {
        Preconditions.checkArgument(flashes >= 0, "Flashes has to be a positive number!");
        ((LightningBoltAccessorMixin) this.getHandle()).paperarc$setFlashes(flashes);
    }

    @Unique
    public org.bukkit.entity.Entity getCausingEntity() {
        net.minecraft.server.level.ServerPlayer cause = this.getHandle().getCause();
        return cause == null ? null : com.ixnah.mc.paperarc.bridge.PaperArcBridge.bukkitEntity(cause);
    }

    @Unique
    public void setCausingPlayer(org.bukkit.entity.Player causingPlayer) {
        this.getHandle().setCause(causingPlayer == null ? null : ((CraftPlayer) causingPlayer).getHandle());
    }
}