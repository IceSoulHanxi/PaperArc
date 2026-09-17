package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.EntityBridge;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

/**
 * Injects Paper's {@code Entity.fixedPose} / {@code freezeLocked} (Expand-Pose-API.patch +
 * Freeze-Tick-Lock-API.patch) and origin ({@code origin}/{@code originWorld},
 * Entity-Origin-API.patch) supplementary fields. Field names match Paper exactly (no
 * {@code paperarc$} prefix) for reflection ABI compatibility. Origin accessors use the
 * Paper NMS method names ({@code setOrigin}/{@code getOriginVector}/{@code getOriginWorld});
 * the other fields have no Paper accessor so carry the {@code paper$} prefix via
 * {@link EntityBridge}.
 */
@Mixin(Entity.class)
public abstract class EntityFieldsMixin implements EntityBridge {

    @Unique
    public boolean fixedPose = false; // Paper

    @Unique
    public boolean freezeLocked = false; // Paper

    @Unique
    public org.bukkit.util.Vector origin; // Paper

    @Unique
    public UUID originWorld; // Paper

    @Override
    public boolean paper$fixedPose() {
        return this.fixedPose;
    }

    @Override
    public void paper$setFixedPose(boolean fixedPose) {
        this.fixedPose = fixedPose;
    }

    @Override
    public boolean paper$freezeLocked() {
        return this.freezeLocked;
    }

    @Override
    public void paper$setFreezeLocked(boolean freezeLocked) {
        this.freezeLocked = freezeLocked;
    }

    @Override
    public void setOrigin(org.bukkit.Location location) {
        this.origin = location.toVector();
        this.originWorld = location.getWorld() != null ? location.getWorld().getUID() : null;
    }

    @Override
    public org.bukkit.util.Vector getOriginVector() {
        return this.origin != null ? this.origin.clone() : null;
    }

    @Override
    public UUID getOriginWorld() {
        return this.originWorld;
    }
}
