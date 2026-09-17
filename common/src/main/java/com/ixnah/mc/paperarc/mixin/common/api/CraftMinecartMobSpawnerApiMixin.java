package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

/**
 * {@code org.bukkit.spawner.Spawner} 的第二个实现类：刷怪笼矿车。
 * {@code CraftCreatureSpawner}（方块）与 {@code CraftMinecartMobSpawner}（实体）
 * 互不继承，只给前者加实现体时矿车一调就 {@code AbstractMethodError}。
 */
// CraftMinecartMobSpawner 是包级私有的 final 类，只能按名字做目标
@Mixin(targets = "org.bukkit.craftbukkit.v.entity.CraftMinecartMobSpawner")
public abstract class CraftMinecartMobSpawnerApiMixin {

    @Unique
    private BaseSpawner paperarc$spawner() {
        return ((MinecartSpawner) ((CraftEntity) (Object) this).getHandle()).getSpawner();
    }

    @Unique
    private net.minecraft.world.level.Level paperarc$level() {
        return ((CraftEntity) (Object) this).getHandle().level();
    }

    @Unique
    private net.minecraft.core.BlockPos paperarc$pos() {
        return ((CraftEntity) (Object) this).getHandle().blockPosition();
    }

    @Unique
    public boolean isActivated() {
        return this.paperarc$spawner().isNearPlayer(this.paperarc$level(), this.paperarc$pos());
    }

    @Unique
    public void resetTimer() {
        this.paperarc$spawner().delay(this.paperarc$level(), this.paperarc$pos());
    }

    @Unique
    public void setSpawnedItem(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null && !itemStack.getType().isAir(),
                "spawners cannot spawn air");
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag entity = new CompoundTag();
        entity.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ITEM).toString());
        entity.put("Item", item.save(this.paperarc$level().registryAccess(), new CompoundTag()));
        this.paperarc$spawner().setNextSpawnData(this.paperarc$level(), this.paperarc$pos(),
                new SpawnData(entity, Optional.empty(), Optional.empty()));
    }
}
