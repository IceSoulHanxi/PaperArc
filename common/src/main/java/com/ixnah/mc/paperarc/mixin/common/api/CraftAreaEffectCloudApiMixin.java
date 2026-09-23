package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.UUID;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.v.entity.CraftAreaEffectCloud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's AreaEffectCloud owner-by-UUID API.
 *
 * Paper implements these by publicizing the private NMS field
 * {@code AreaEffectCloud.ownerUUID} via an access transformer;
 * {@code paperarc.accesswidener} opens it the same way.
 */
@Mixin(CraftAreaEffectCloud.class)
public abstract class CraftAreaEffectCloudApiMixin {

    @Shadow
    public abstract AreaEffectCloud getHandle();

    @Unique
    public UUID getOwnerUniqueId() {
        net.minecraft.world.entity.EntityReference<LivingEntity> ref = getHandle().owner;
        if (ref != null) {
            return ref.getUUID();
        }
        LivingEntity owner = getHandle().getOwner();
        return owner != null ? owner.getUUID() : null;
    }

    @Unique
    public void setOwnerUniqueId(UUID ownerUuid) {
        getHandle().owner = ownerUuid != null ? net.minecraft.world.entity.EntityReference.of(ownerUuid) : null;
    }
}
