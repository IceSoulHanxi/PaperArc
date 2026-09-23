package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.craftbukkit.v.entity.CraftTameableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** Paper 的 {@code Tameable#getOwnerUniqueId()}：不解析实体、只取 NMS 存的 UUID。 */
@Mixin(CraftTameableAnimal.class)
public abstract class CraftTameableAnimalApiMixin {

    @Shadow
    public abstract TamableAnimal getHandle();

    @Unique
    public java.util.UUID getOwnerUniqueId() {
        var ref = this.getHandle().getOwnerReference();
        return ref != null ? ref.getUUID() : null;
    }
}
