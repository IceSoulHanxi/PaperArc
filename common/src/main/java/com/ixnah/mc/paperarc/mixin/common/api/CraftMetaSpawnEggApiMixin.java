package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;

import org.bukkit.craftbukkit.v.inventory.CraftMetaSpawnEgg;
import org.bukkit.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's {@code Fix-SpawnEggMeta-get-setSpawnedType.patch} additions to
 * {@link CraftMetaSpawnEgg}: the non-deprecated {@code getCustomSpawnedType()}
 * and {@code setCustomSpawnedType(EntityType)} accessors that bypass the legacy
 * material validation and read/write the {@code EntityTag} NBT directly.
 *
 * <p>{@code entityTag} is a private field here, shadowed directly; the
 * {@code id} tag key matches the vanilla {@code ENTITY_ID} NBT key.</p>
 */
@Mixin(CraftMetaSpawnEgg.class)
public abstract class CraftMetaSpawnEggApiMixin {

    @Shadow
    private CompoundTag entityTag;

    @Unique
    public EntityType getCustomSpawnedType() {
        return Optional.ofNullable(this.entityTag)
                .map(tag -> tag.getString("id"))
                .flatMap(net.minecraft.world.entity.EntityType::byString)
                .map(CraftMetaSpawnEggApiMixin::paperarc$toBukkit)
                .orElse(null);
    }

    @Unique
    public void setCustomSpawnedType(EntityType type) {
        if (type == null) {
            if (this.entityTag != null) {
                this.entityTag.remove("id");
            }
        } else {
            if (this.entityTag == null) {
                this.entityTag = new CompoundTag();
            }
            this.entityTag.putString("id", type.getKey().toString());
        }
    }

    @Unique
    private static EntityType paperarc$toBukkit(net.minecraft.world.entity.EntityType<?> nms) {
        return org.bukkit.Registry.ENTITY_TYPE.get(
                org.bukkit.craftbukkit.v.util.CraftNamespacedKey.fromMinecraft(
                        net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(nms)));
    }
}
