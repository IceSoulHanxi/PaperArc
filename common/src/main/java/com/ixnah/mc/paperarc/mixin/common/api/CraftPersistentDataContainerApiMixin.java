package com.ixnah.mc.paperarc.mixin.common.api;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

/**
 * Adds readFromBytes missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Added-byte-array-serialization-deserialization-for-P.patch.
 * Paper calls this.clear() which Arclight lacks too -> shadowed internal tag map cleared directly.
 */
@Mixin(CraftPersistentDataContainer.class)
public abstract class CraftPersistentDataContainerApiMixin {

    @Shadow
    private Map<String, Tag> customDataTags;

    @Shadow
    public abstract void putAll(Map<String, Tag> map);

    @Unique
    public void readFromBytes(byte[] bytes, boolean clear) throws IOException {
        if (clear) {
            this.customDataTags.clear();
        }
        try (DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(bytes))) {
            CompoundTag compound = NbtIo.read(dataInput);
            // compound.tags opened via paperarc.accesswidener (project rule: AW over reflection)
            this.putAll(compound.tags);
        }
    }

    /**
     * Paper's PersistentDataContainer {@code has(NamespacedKey)} overload; the
     * tag map is keyed by the full key string.
     */
    @Unique
    public boolean has(org.bukkit.NamespacedKey key) {
        return this.customDataTags.containsKey(key.toString());
    }
}
