package dev.paperarc.mixin.common.api;

import java.lang.reflect.Field;
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
 * {@code AreaEffectCloud.ownerUUID} via an access transformer. Since a mixin on
 * the Craft host cannot reach into the NMS class' privates, the field is accessed
 * reflectively (runtime mappings are mojmap, where the field is named ownerUUID).
 */
@Mixin(CraftAreaEffectCloud.class)
public abstract class CraftAreaEffectCloudApiMixin {

    @Shadow
    public abstract AreaEffectCloud getHandle();

    @Unique
    private static volatile Field PAPERARC$OWNER_UUID_FIELD;

    @Unique
    private static Field paperarc$ownerUuidField() {
        Field f = PAPERARC$OWNER_UUID_FIELD;
        if (f == null) {
            synchronized (CraftAreaEffectCloudApiMixin.class) {
                if (PAPERARC$OWNER_UUID_FIELD == null) {
                    try {
                        Field resolved = AreaEffectCloud.class.getDeclaredField("ownerUUID");
                        resolved.setAccessible(true);
                        PAPERARC$OWNER_UUID_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS AreaEffectCloud.ownerUUID field not found", e);
                    }
                }
                f = PAPERARC$OWNER_UUID_FIELD;
            }
        }
        return f;
    }

    @Unique
    public UUID getOwnerUniqueId() {
        LivingEntity owner = getHandle().getOwner();
        UUID cached = paperarc$getRawOwnerUuid();
        if (cached != null) {
            return cached;
        }
        return owner != null ? owner.getUUID() : null;
    }

    @Unique
    private UUID paperarc$getRawOwnerUuid() {
        try {
            return (UUID) paperarc$ownerUuidField().get(getHandle());
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Unique
    public void setOwnerUniqueId(UUID ownerUuid) {
        // Mirror Paper: clear any resolved entity reference first, then store the raw UUID.
        getHandle().setOwner((LivingEntity) null);
        if (ownerUuid != null) {
            try {
                paperarc$ownerUuidField().set(getHandle(), ownerUuid);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to set NMS AreaEffectCloud.ownerUUID", e);
            }
        }
    }
}
