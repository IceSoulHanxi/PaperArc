package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.TypedEntityData;
import org.bukkit.craftbukkit.v.entity.CraftEntityType;
import org.bukkit.craftbukkit.v.inventory.CraftMetaSpawnEgg;
import org.bukkit.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's additions to {@link CraftMetaSpawnEgg}: the non-deprecated
 * {@code getCustomSpawnedType()} and {@code setCustomSpawnedType(EntityType)}
 * accessors.
 */
@Mixin(CraftMetaSpawnEgg.class)
public abstract class CraftMetaSpawnEggApiMixin {

    @Shadow
    private TypedEntityData<net.minecraft.world.entity.EntityType<?>> entityTag;

    @Unique
    public EntityType getCustomSpawnedType() {
        if (this.entityTag == null) {
            return null;
        }
        return CraftEntityType.minecraftToBukkit(this.entityTag.type());
    }

    @Unique
    public void setCustomSpawnedType(EntityType type) {
        if (type == null) {
            this.entityTag = null;
        } else {
            CompoundTag tag = this.entityTag != null ? this.entityTag.copyTagWithoutId() : new CompoundTag();
            this.entityTag = TypedEntityData.of(CraftEntityType.bukkitToMinecraft(type), tag);
        }
    }
}
