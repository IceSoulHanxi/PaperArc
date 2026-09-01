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
 * {@code AreaEffectCloud.ownerUUID} via an access transformer; here it is
 * widened via AT (f_19695_) and accessed directly — no reflection.
 */
@Mixin(CraftAreaEffectCloud.class)
public abstract class CraftAreaEffectCloudApiMixin {

    @Shadow
    public abstract AreaEffectCloud getHandle();

    @Unique
    public UUID getOwnerUniqueId() {
        LivingEntity owner = getHandle().getOwner();
        UUID cached = getHandle().ownerUUID;
        if (cached != null) {
            return cached;
        }
        return owner != null ? owner.getUUID() : null;
    }

    @Unique
    public void setOwnerUniqueId(UUID ownerUuid) {
        // Mirror Paper: clear any resolved entity reference first, then store the raw UUID.
        getHandle().setOwner((LivingEntity) null);
        getHandle().ownerUUID = ownerUuid;
    }
}
