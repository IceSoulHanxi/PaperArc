package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.EntityBridge;
import com.ixnah.mc.paperarc.bridge.PaperarcLootableData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Injects Paper's {@code Entity.fixedPose} / {@code freezeLocked} (Expand-Pose-API.patch +
 * Freeze-Tick-Lock-API.patch) and origin ({@code origin}/{@code originWorld},
 * Entity-Origin-API.patch) supplementary fields. Field names match Paper exactly (no
 * {@code paperarc$} prefix) for reflection ABI compatibility. Origin accessors use the
 * Paper NMS method names ({@code setOrigin}/{@code getOriginVector}/{@code getOriginWorld});
 * the other fields have no Paper accessor so carry the {@code paper$} prefix via
 * {@link EntityBridge}.
 *
 * <p>A8/Y-3 追加 {@code spawnedViaMobSpawner}/{@code spawnReason}（E6）与这一族字段的
 * **NBT 落盘**：Paper 把它们写在 {@code Entity#saveWithoutId}/{@code load} 这一层
 * （所有实体的唯一汇聚点，子类的 {@code addAdditionalSaveData} 都在它里面被调用），
 * 键名与 Paper 完全一致（{@code Paper.FromMobSpawner}/{@code Paper.SpawnReason}/
 * {@code Paper.FrictionState}/{@code Paper.Origin}/{@code Paper.OriginWorld}），
 * 以便和真实 Paper 存档互通。载具/箱船的 Lootable 记账同样挂在这里。
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

    @Unique
    public net.kyori.adventure.util.TriState frictionState = net.kyori.adventure.util.TriState.NOT_SET; // Paper

    @Unique
    public boolean spawnedViaMobSpawner; // Paper

    @Unique
    public CreatureSpawnEvent.SpawnReason spawnReason; // Paper

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

    @Override
    public net.kyori.adventure.util.TriState paper$frictionState() {
        return this.frictionState;
    }

    @Override
    public void paper$setFrictionState(net.kyori.adventure.util.TriState state) {
        this.frictionState = state == null ? net.kyori.adventure.util.TriState.NOT_SET : state;
    }

    @Override
    public boolean paper$spawnedViaMobSpawner() {
        return this.spawnedViaMobSpawner;
    }

    @Override
    public void paper$setSpawnedViaMobSpawner(boolean spawnedViaMobSpawner) {
        this.spawnedViaMobSpawner = spawnedViaMobSpawner;
    }

    @Override
    public CreatureSpawnEvent.SpawnReason paper$spawnReason() {
        return this.spawnReason;
    }

    @Override
    public void paper$setSpawnReason(CreatureSpawnEvent.SpawnReason reason) {
        this.spawnReason = reason;
    }

    // ---- NBT 落盘（A8/Y-3）----

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void paperarc$savePaperFields(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.spawnedViaMobSpawner) {
            nbt.putBoolean("Paper.FromMobSpawner", true);
        }
        if (this.spawnReason != null) {
            nbt.putString("Paper.SpawnReason", this.spawnReason.name());
        }
        if (this.frictionState != net.kyori.adventure.util.TriState.NOT_SET) {
            nbt.putString("Paper.FrictionState", this.frictionState.toString());
        }
        if (this.origin != null) {
            if (this.originWorld != null) {
                nbt.putUUID("Paper.OriginWorld", this.originWorld);
            }
            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(this.origin.getX()));
            pos.add(DoubleTag.valueOf(this.origin.getY()));
            pos.add(DoubleTag.valueOf(this.origin.getZ()));
            nbt.put("Paper.Origin", pos);
        }
        PaperarcLootableData.saveIfPresent(this, nbt);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void paperarc$loadPaperFields(CompoundTag nbt, CallbackInfo ci) {
        this.spawnedViaMobSpawner = nbt.getBoolean("Paper.FromMobSpawner");
        if (nbt.contains("Paper.SpawnReason", Tag.TAG_STRING)) {
            try {
                this.spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(nbt.getString("Paper.SpawnReason"));
            } catch (IllegalArgumentException ignored) {
                // 未知枚举名（跨版本存档）：按下面的推导兜底，与 Paper 一样不打断加载
            }
        }
        if (this.spawnReason == null) {
            // 与 Paper 的推导一致：老存档没有这个键时按实体形态猜一个，别一律 DEFAULT
            if (this.spawnedViaMobSpawner) {
                this.spawnReason = CreatureSpawnEvent.SpawnReason.SPAWNER;
            } else if ((Object) this instanceof Mob mob
                    && ((Object) this instanceof Animal || (Object) this instanceof AbstractFish)
                    && !mob.removeWhenFarAway(0.0D)
                    && !nbt.getBoolean("PersistenceRequired")) {
                this.spawnReason = CreatureSpawnEvent.SpawnReason.NATURAL;
            } else {
                this.spawnReason = CreatureSpawnEvent.SpawnReason.DEFAULT;
            }
        }
        if (nbt.contains("Paper.FrictionState", Tag.TAG_STRING)) {
            try {
                this.frictionState =
                        net.kyori.adventure.util.TriState.valueOf(nbt.getString("Paper.FrictionState"));
            } catch (IllegalArgumentException ignored) {
                // 同上
            }
        }
        if (nbt.contains("Paper.OriginWorld")) {
            this.originWorld = nbt.getUUID("Paper.OriginWorld");
        }
        if (nbt.contains("Paper.Origin", Tag.TAG_LIST)) {
            ListTag pos = nbt.getList("Paper.Origin", Tag.TAG_DOUBLE);
            if (pos.size() == 3) {
                this.origin = new org.bukkit.util.Vector(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2));
            }
        }
        PaperarcLootableData.loadIfPresent(this, nbt);
    }
}
