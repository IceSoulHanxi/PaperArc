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
 * B8/Y-3：自定义祭坛已挪到 NMS {@code EnderDragon} 上（{@code entity.EnderDragonFieldsMixin}），
 * 与 Paper 同宿主；没设过时按 Paper 的公式用 {@code EndPodiumFeature.getLocation(fightOrigin)}。
 * 与 Paper 一样不落盘。
 */
@Mixin(CraftEnderDragon.class)
public abstract class CraftEnderDragonApiMixin {

    @Shadow
    public abstract EnderDragon getHandle();

    @Unique
    private World paperarc$world() {
        return ((CraftEntity) (Object) this).getWorld();
    }

    @Unique
    public Location getPodium() {
        BlockPos custom = ((com.ixnah.mc.paperarc.bridge.EnderDragonPodiumBridge) getHandle()).paper$getPodium();
        BlockPos pos = custom != null
            ? custom
            : EndPodiumFeature.getLocation(getHandle().getFightOrigin());
        return new Location(paperarc$world(), pos.getX(), pos.getY(), pos.getZ());
    }

    @Unique
    public void setPodium(@Nullable Location location) {
        com.ixnah.mc.paperarc.bridge.EnderDragonPodiumBridge bridge =
                (com.ixnah.mc.paperarc.bridge.EnderDragonPodiumBridge) getHandle();
        if (location == null) {
            bridge.paper$setPodium(null);
            return;
        }
        if (location.getWorld() != null && !location.getWorld().equals(paperarc$world())) {
            throw new IllegalArgumentException("You cannot set a podium in a different world to where the dragon is");
        }
        bridge.paper$setPodium(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}
