package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EntityBridge;
import com.ixnah.mc.paperarc.bridge.craft.PaperarcLootableData;
import net.kyori.adventure.util.TriState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把我们加在实体上的 Paper 补充字段写进 / 读出实体 NBT（gaps.md §3.1 持久化行）。
 *
 * <p>为什么挂在 {@code Entity#saveWithoutId}/{@code load} 而不是各子类的
 * {@code addAdditionalSaveData}：这几样状态分散在 {@code Entity}（origin、spawnReason）、
 * {@code LivingEntity} 与 {@code ItemEntity}（frictionState）、以及矿车/箱船
 * （lootable 记账）上，一处收口最省事，也与 CraftBukkit 写 {@code BukkitValues} 的位置一致。
 *
 * <p>键名沿用 Paper（{@code Paper.Origin}/{@code Paper.OriginWorld}/
 * {@code Paper.FrictionState}）与 Spigot（{@code Spigot.ticksLived} 那一族的
 * {@code Bukkit.*}）的命名习惯；只在值不是默认值时才写，避免给每个实体都加标签。
 */
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
    private void paperarc$saveSupplementary(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        EntityBridge bridge = (EntityBridge) this;
        org.bukkit.util.Vector origin = bridge.getOriginVector();
        if (origin != null) {
            nbt.put(PAPERARC_ORIGIN, paperarc$vectorTag(origin));
            java.util.UUID world = bridge.getOriginWorld();
            if (world != null) {
                nbt.putUUID(PAPERARC_ORIGIN_WORLD, world);
            }
        }
        CreatureSpawnEvent.SpawnReason reason = bridge.paper$spawnReason();
        if (reason != null && reason != CreatureSpawnEvent.SpawnReason.DEFAULT) {
            nbt.putString(PAPERARC_SPAWN_REASON, reason.name());
        }
        if (bridge.paper$spawnedViaMobSpawner()) {
            nbt.putBoolean(PAPERARC_FROM_SPAWNER, true);
        }
        TriState friction = paperarc$friction();
        if (friction != null && friction != TriState.NOT_SET) {
            nbt.putString(PAPERARC_FRICTION, friction.name());
        }
        PaperarcLootableData.saveIfPresent(this, nbt);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void paperarc$loadSupplementary(CompoundTag nbt, CallbackInfo ci) {
        EntityBridge bridge = (EntityBridge) this;
        if (nbt.contains(PAPERARC_ORIGIN, Tag.TAG_LIST) && nbt.hasUUID(PAPERARC_ORIGIN_WORLD)) {
            net.minecraft.nbt.ListTag list = nbt.getList(PAPERARC_ORIGIN, Tag.TAG_DOUBLE);
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(nbt.getUUID(PAPERARC_ORIGIN_WORLD));
            if (list.size() == 3 && world != null) {
                bridge.setOrigin(new org.bukkit.Location(world,
                        list.getDouble(0), list.getDouble(1), list.getDouble(2)));
            }
        }
        if (nbt.contains(PAPERARC_SPAWN_REASON, Tag.TAG_STRING)) {
            try {
                bridge.paper$setSpawnReason(
                        CreatureSpawnEvent.SpawnReason.valueOf(nbt.getString(PAPERARC_SPAWN_REASON)));
            } catch (IllegalArgumentException ignored) {
                // 存档里是别的版本写的原因名，按默认处理即可
            }
        }
        if (nbt.getBoolean(PAPERARC_FROM_SPAWNER)) {
            bridge.paper$setSpawnedViaMobSpawner(true);
        }
        if (nbt.contains(PAPERARC_FRICTION, Tag.TAG_STRING)) {
            try {
                paperarc$setFriction(TriState.valueOf(nbt.getString(PAPERARC_FRICTION)));
            } catch (IllegalArgumentException ignored) {
                // 同上
            }
        }
        PaperarcLootableData.loadIfPresent(this, nbt);
    }

    @Unique
    private static net.minecraft.nbt.ListTag paperarc$vectorTag(org.bukkit.util.Vector vec) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        list.add(net.minecraft.nbt.DoubleTag.valueOf(vec.getX()));
        list.add(net.minecraft.nbt.DoubleTag.valueOf(vec.getY()));
        list.add(net.minecraft.nbt.DoubleTag.valueOf(vec.getZ()));
        return list;
    }

    /** frictionState 只存在于 LivingEntity / ItemEntity 两条线上，各有各的 bridge。 */
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
