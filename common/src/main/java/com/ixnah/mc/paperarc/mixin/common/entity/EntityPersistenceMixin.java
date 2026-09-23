package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EntityBridge;
import com.ixnah.mc.paperarc.bridge.craft.PaperarcLootableData;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityPersistenceMixin {

    @Unique
    private static final String PAPERARC_ORIGIN = "Paper.Origin";

    @Unique
    private static final String PAPERARC_ORIGIN_WORLD = "Paper.OriginWorld";

    @Unique
    private static final String PAPERARC_FRICTION = "Paper.FrictionState";

    @Unique
    private static final String PAPERARC_SPAWN_REASON = "Bukkit.spawnReason";

    @Unique
    private static final String PAPERARC_FROM_SPAWNER = "Paper.FromMobSpawner";

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void paperarc$saveSupplementary(ValueOutput out, CallbackInfo ci) {
        EntityBridge bridge = (EntityBridge) this;
        org.bukkit.util.Vector origin = bridge.getOriginVector();
        if (origin != null) {
            out.store(PAPERARC_ORIGIN, Vec3.CODEC, new Vec3(origin.getX(), origin.getY(), origin.getZ()));
            UUID world = bridge.getOriginWorld();
            if (world != null) {
                out.store(PAPERARC_ORIGIN_WORLD, UUIDUtil.CODEC, world);
            }
        }
        CreatureSpawnEvent.SpawnReason reason = bridge.paper$spawnReason();
        if (reason != null && reason != CreatureSpawnEvent.SpawnReason.DEFAULT) {
            out.putString(PAPERARC_SPAWN_REASON, reason.name());
        }
        if (bridge.paper$spawnedViaMobSpawner()) {
            out.putBoolean(PAPERARC_FROM_SPAWNER, true);
        }
        TriState friction = paperarc$friction();
        if (friction != null && friction != TriState.NOT_SET) {
            out.putString(PAPERARC_FRICTION, friction.name());
        }
        PaperarcLootableData.saveIfPresent(this, out);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void paperarc$loadSupplementary(ValueInput in, CallbackInfo ci) {
        EntityBridge bridge = (EntityBridge) this;
        Optional<Vec3> originOpt = in.read(PAPERARC_ORIGIN, Vec3.CODEC);
        Optional<UUID> worldOpt = in.read(PAPERARC_ORIGIN_WORLD, UUIDUtil.CODEC);
        if (originOpt.isPresent() && worldOpt.isPresent()) {
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldOpt.get());
            if (world != null) {
                Vec3 vec = originOpt.get();
                bridge.setOrigin(new org.bukkit.Location(world, vec.x, vec.y, vec.z));
            }
        }
        in.getString(PAPERARC_SPAWN_REASON).ifPresent(str -> {
            try {
                bridge.paper$setSpawnReason(CreatureSpawnEvent.SpawnReason.valueOf(str));
            } catch (IllegalArgumentException ignored) {
            }
        });
        if (in.getBooleanOr(PAPERARC_FROM_SPAWNER, false)) {
            bridge.paper$setSpawnedViaMobSpawner(true);
        }
        in.getString(PAPERARC_FRICTION).ifPresent(str -> {
            try {
                paperarc$setFriction(TriState.valueOf(str));
            } catch (IllegalArgumentException ignored) {
            }
        });
        PaperarcLootableData.loadIfPresent(this, in);
    }

    @Unique
    private TriState paperarc$friction() {
        if (this instanceof com.ixnah.mc.paperarc.bridge.LivingEntityFieldsBridge living) {
            return living.paper$getFrictionState();
        }
        if (this instanceof com.ixnah.mc.paperarc.bridge.ItemEntityBridge item) {
            return item.paper$getFrictionState();
        }
        return null;
    }

    @Unique
    private void paperarc$setFriction(TriState state) {
        if (this instanceof com.ixnah.mc.paperarc.bridge.LivingEntityFieldsBridge living) {
            living.paper$setFrictionState(state);
        } else if (this instanceof com.ixnah.mc.paperarc.bridge.ItemEntityBridge item) {
            item.paper$setFrictionState(state);
        }
    }
}
