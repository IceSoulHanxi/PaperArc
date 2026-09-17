package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.v.boss.CraftDragonBattle;
import org.bukkit.entity.EnderCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adds Paper's DragonBattle API (More-DragonBattle-API).
 *
 * Vanilla NMS keeps {@code gateways}, {@code respawnCrystals}, {@code GATEWAY_COUNT}
 * and both {@code spawnNewGateway} overloads private, so they are accessed via
 * reflection. Paper's NMS-side helpers ({@code spawnNewGatewayIfPossible},
 * {@code getSpikeCrystals}) are mirrored locally. The CB-added {@code valid} flag on
 * NMS entities does not exist in the runtime NMS jar, so crystal liveness checks use
 * only {@code !isRemoved() && isAlive()}.
 */
@Mixin(CraftDragonBattle.class)
public abstract class CraftDragonBattleApiMixin {

    @Shadow
    private EndDragonFight handle;

    // EndDragonFight 的 GATEWAY_COUNT/gateways/respawnCrystals/level 与两个
    // spawnNewGateway 重载都是 vanilla 私有成员，统一由 paperarc.accesswidener 放开。

    @Unique
    public int getGatewayCount() {
        return EndDragonFight.GATEWAY_COUNT - this.handle.gateways.size();
    }

    @Unique
    public boolean spawnNewGateway() {
        if (!this.handle.gateways.isEmpty()) {
            this.handle.spawnNewGateway();
            return true;
        }
        return false;
    }

    @Unique
    public void spawnNewGateway(io.papermc.paper.math.Position position) {
        this.handle.spawnNewGateway(BlockPos.containing(position.x(), position.y(), position.z()));
    }

    @Unique
    public List<EnderCrystal> getRespawnCrystals() {
        List<EndCrystal> crystals = this.handle.respawnCrystals;
        if (crystals == null) {
            return Collections.emptyList();
        }
        List<EnderCrystal> enderCrystals = new ArrayList<>();
        for (EndCrystal endCrystal : crystals) {
            if (!endCrystal.isRemoved() && endCrystal.isAlive()) {
                enderCrystals.add((EnderCrystal) PaperArcBridge.bukkitEntity(endCrystal));
            }
        }
        return Collections.unmodifiableList(enderCrystals);
    }

    @Unique
    public List<EnderCrystal> getHealingCrystals() {
        ServerLevel level = this.handle.level;
        List<EnderCrystal> enderCrystals = new ArrayList<>();
        // Mirror of Paper's NMS getSpikeCrystals().
        for (net.minecraft.world.level.levelgen.feature.SpikeFeature.EndSpike spike :
                net.minecraft.world.level.levelgen.feature.SpikeFeature.getSpikesForLevel(level)) {
            AABB box = spike.getTopBoundingBox();
            for (EndCrystal endCrystal : level.getEntitiesOfClass(EndCrystal.class, box)) {
                if (!endCrystal.isRemoved() && endCrystal.isAlive()) {
                    enderCrystals.add((EnderCrystal) PaperArcBridge.bukkitEntity(endCrystal));
                }
            }
        }
        return Collections.unmodifiableList(enderCrystals);
    }
}
