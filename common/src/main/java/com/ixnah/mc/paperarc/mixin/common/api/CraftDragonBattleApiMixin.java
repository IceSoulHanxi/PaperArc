package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.v1_20_R1.boss.CraftDragonBattle;
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
 * Vanilla NMS keeps {@code gateways}, {@code respawnCrystals}, {@code GATEWAY_COUNT},
 * {@code level} and both {@code spawnNewGateway} overloads private; they are widened
 * via AT (f_156741_ / f_64062_ / f_64061_ / f_64075_ / m_64089_ / m_64109_) and
 * accessed directly — no reflection. Paper's NMS-side helpers
 * ({@code spawnNewGatewayIfPossible}, {@code getSpikeCrystals}) are mirrored locally.
 * The CB-added {@code valid} flag on NMS entities does not exist in the runtime NMS
 * jar, so crystal liveness checks use only {@code !isRemoved() && isAlive()}.
 */
@Mixin(CraftDragonBattle.class)
public abstract class CraftDragonBattleApiMixin {

    @Shadow
    private EndDragonFight handle;

    @Unique
    @SuppressWarnings("unchecked")
    private List<Integer> paperarc$gateways() {
        return (List<Integer>) this.handle.gateways;
    }

    @Unique
    @SuppressWarnings("unchecked")
    private List<EndCrystal> paperarc$respawnCrystals() {
        return (List<EndCrystal>) this.handle.respawnCrystals;
    }

    @Unique
    private void paperarc$spawnNewGateway(BlockPos pos) {
        this.handle.spawnNewGateway(pos);
    }

    @Unique
    public int getGatewayCount() {
        return EndDragonFight.GATEWAY_COUNT - this.paperarc$gateways().size();
    }

    @Unique
    public boolean spawnNewGateway() {
        if (!this.paperarc$gateways().isEmpty()) {
            this.handle.spawnNewGateway();
            return true;
        }
        return false;
    }

    @Unique
    public void spawnNewGateway(io.papermc.paper.math.Position position) {
        this.paperarc$spawnNewGateway(BlockPos.containing(position.x(), position.y(), position.z()));
    }

    @Unique
    public List<EnderCrystal> getRespawnCrystals() {
        List<EndCrystal> crystals = this.paperarc$respawnCrystals();
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
