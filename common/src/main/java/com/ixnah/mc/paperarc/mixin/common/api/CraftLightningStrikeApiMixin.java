package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.LightningBolt;
import org.bukkit.craftbukkit.v.entity.CraftLightningStrike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;


/**
 * Port of Paper's More-lightning-API additions on
 * {@link CraftLightningStrike}: {@code getFlashCount()},
 * {@code setFlashCount(int)} and {@code getCausingEntity()}.
 *
 * Vanilla NMS keeps the {@code flashes} counter private (Paper widens it via
 * AT); {@code paperarc.accesswidener} opens it. {@code getCause()} is public vanilla.
 */
@Mixin(CraftLightningStrike.class)
public abstract class CraftLightningStrikeApiMixin {

    @Shadow
    public abstract LightningBolt getHandle();

    @Unique
    public int getFlashCount() {
        return this.getHandle().flashes;
    }

    @Unique
    public void setFlashCount(int flashes) {
        Preconditions.checkArgument(flashes >= 0, "Flashes has to be a positive number!");
        this.getHandle().flashes = flashes;
    }

    @Unique
    public org.bukkit.entity.Entity getCausingEntity() {
        net.minecraft.server.level.ServerPlayer cause = this.getHandle().getCause();
        return cause == null ? null : com.ixnah.mc.paperarc.bridge.PaperArcBridge.bukkitEntity(cause);
    }
}
