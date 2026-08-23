package dev.paperarc.mixin.common.api;

import dev.paperarc.bridge.PaperArcBridge;
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

    @Unique
    private static int paperarc$gatewayCount() {
        try {
            Field f = EndDragonFight.class.getDeclaredField("GATEWAY_COUNT");
            f.setAccessible(true);
            return f.getInt(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS EndDragonFight.GATEWAY_COUNT not found", e);
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    private List<Integer> paperarc$gateways() {
        try {
            Field f = EndDragonFight.class.getDeclaredField("gateways");
            f.setAccessible(true);
            return (List<Integer>) f.get(this.handle);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS EndDragonFight.gateways not found", e);
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    private List<EndCrystal> paperarc$respawnCrystals() {
        try {
            Field f = EndDragonFight.class.getDeclaredField("respawnCrystals");
            f.setAccessible(true);
            return (List<EndCrystal>) f.get(this.handle);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS EndDragonFight.respawnCrystals not found", e);
        }
    }

    @Unique
    private void paperarc$spawnNewGateway(BlockPos pos) {
        try {
            Method m = EndDragonFight.class.getDeclaredMethod("spawnNewGateway", BlockPos.class);
            m.setAccessible(true);
            m.invoke(this.handle, pos);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS EndDragonFight.spawnNewGateway(BlockPos) not found", e);
        }
    }

    @Unique
    public int getGatewayCount() {
        return paperarc$gatewayCount() - this.paperarc$gateways().size();
    }

    @Unique
    public boolean spawnNewGateway() {
        if (!this.paperarc$gateways().isEmpty()) {
            try {
                Method m = EndDragonFight.class.getDeclaredMethod("spawnNewGateway");
                m.setAccessible(true);
                m.invoke(this.handle);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("NMS EndDragonFight.spawnNewGateway() not found", e);
            }
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
        ServerLevel level;
        try {
            Field f = EndDragonFight.class.getDeclaredField("level");
            f.setAccessible(true);
            level = (ServerLevel) f.get(this.handle);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS EndDragonFight.level not found", e);
        }
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
