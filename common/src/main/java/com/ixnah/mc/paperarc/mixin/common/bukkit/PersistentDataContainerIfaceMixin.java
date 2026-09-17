package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.persistence.PersistentDataContainer} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.persistence.PersistentDataContainer", remap = false)
public interface PersistentDataContainerIfaceMixin {

    @Unique
    public abstract boolean has(org.bukkit.NamespacedKey p0);

    // 实现体在 CraftPersistentDataContainerApiMixin（A4-1 NO_DECL）。
    @Unique
    public abstract void readFromBytes(byte[] p0, boolean p1) throws java.io.IOException;

    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default void readFromBytes(byte[] bytes) throws java.io.IOException {
        readFromBytes(bytes, true);
    }

}