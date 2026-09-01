package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.Optional;

import com.google.common.base.Preconditions;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

import org.bukkit.craftbukkit.v.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Mob-Spawner-API-Enhancements methods to {@link CraftCreatureSpawner}:
 * {@code isActivated()}, {@code resetTimer()} and {@code setSpawnedItem(ItemStack)}.
 *
 * <p>{@code BaseSpawner#isNearPlayer}/{@code delay}/{@code setNextSpawnData} are
 * private/protected in vanilla and widened via AT (m_151343_ / m_151350_ / m_142667_).
 * The protected {@code getSnapshot()} / {@code requirePlaced()} / {@code getPosition()}
 * members of the CraftBlockState hierarchy are reached through the
 * {@link CraftBlockStateBridge} / {@link CraftBlockEntityStateBridge} provider mixins
 * instead of subclass @Shadow.</p>
 */
@Mixin(CraftCreatureSpawner.class)
public abstract class CraftCreatureSpawnerApiMixin {

    @Unique
    private void paperarc$requirePlaced() {
        Preconditions.checkState(((CraftBlockStateBridge) (Object) this).paperarc$isPlaced(),
                "Tile must be placed to apply changes");
    }

    @Unique
    private SpawnerBlockEntity paperarc$spawnerSnapshot() {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        return snapshot instanceof SpawnerBlockEntity sbe ? sbe : null;
    }

    @Unique
    public boolean isActivated() {
        this.paperarc$requirePlaced();
        SpawnerBlockEntity snapshot = this.paperarc$spawnerSnapshot();
        return snapshot != null && snapshot.getSpawner().isNearPlayer(
                this.paperarc$world(), this.paperarc$position());
    }

    @Unique
    public void resetTimer() {
        this.paperarc$requirePlaced();
        SpawnerBlockEntity snapshot = this.paperarc$spawnerSnapshot();
        if (snapshot != null) {
            snapshot.getSpawner().delay(this.paperarc$world(), this.paperarc$position());
        }
    }

    @Unique
    public void setSpawnedItem(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null && !itemStack.getType().isAir(),
                "spawners cannot spawn air");
        SpawnerBlockEntity snapshot = this.paperarc$spawnerSnapshot();
        if (snapshot == null) {
            return;
        }
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag entity = new CompoundTag();
        entity.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ITEM).toString());
        entity.put("Item", item.save(new CompoundTag()));
        BaseSpawner spawner = snapshot.getSpawner();
        spawner.setNextSpawnData(((CraftBlockStateBridge) (Object) this).paperarc$isPlaced()
                        ? this.paperarc$world() : null,
                this.paperarc$position(), new SpawnData(entity, Optional.empty()));
    }

    @Unique
    private ServerLevel paperarc$world() {
        org.bukkit.World world = ((CraftBlockStateBridge) (Object) this).paperarc$getWorld();
        return world instanceof org.bukkit.craftbukkit.v.CraftWorld cw ? cw.getHandle() : null;
    }

    @Unique
    private BlockPos paperarc$position() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$getPosition();
    }
}
