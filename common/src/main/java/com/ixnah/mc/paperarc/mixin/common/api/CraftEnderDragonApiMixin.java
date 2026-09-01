package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's EnderDragon podium API.
 *
 * Paper stores the custom podium in a new private NMS field {@code EnderDragon.podium}
 * (vanilla 1.21.1 has none) and falls back to {@code EndPodiumFeature.getLocation(fightOrigin)}.
 * Since the Craft-host mixin cannot declare NMS fields, the custom podium lives in
 * com.ixnah.mc.paperarc.bridge.ApiState; the vanilla default is computed exactly like Paper.
 */
@Mixin(CraftEnderDragon.class)
public abstract class CraftEnderDragonApiMixin {

    // Paper stores the custom podium in a private NMS field; here it lives in an
    // injected Craft field (vanilla default computed like Paper).
    @Unique
    private BlockPos podium;

    @Shadow
    public abstract EnderDragon getHandle();

    @Unique
    private World paperarc$world() {
        return ((CraftEntity) (Object) this).getWorld();
    }

    @Unique
    public Location getPodium() {
        BlockPos pos = this.podium != null
            ? this.podium
            : EndPodiumFeature.getLocation(getHandle().getFightOrigin());
        return new Location(paperarc$world(), pos.getX(), pos.getY(), pos.getZ());
    }

    @Unique
    public void setPodium(@Nullable Location location) {
        if (location == null) {
            this.podium = null;
            return;
        }
        if (location.getWorld() != null && !location.getWorld().equals(paperarc$world())) {
            throw new IllegalArgumentException("You cannot set a podium in a different world to where the dragon is");
        }
        this.podium = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
