package com.ixnah.mc.paperarc.mixin.common.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer;
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
        // paper 写的是 gzip 压缩的 NBT（serializeToBytes 用 writeCompressed），
        // 这里原先用不压缩的 NbtIo.read，自家写出来的字节自己都读不回来
        // （B3-3 探针 P20 往返实测 "Loading NBT data"）。
        CompoundTag compound = NbtIo.readCompressed(new ByteArrayInputStream(bytes),
                net.minecraft.nbt.NbtAccounter.unlimitedHeap());
        // compound.tags opened via paperarc.accesswidener (project rule: AW over reflection)
        this.putAll(compound.tags);
    }

    /**
     * paper {@code PersistentDataContainerView#serializeToBytes}（B3-2），与已有的
     * {@code readFromBytes} 成对：写的是 gzip 压缩的 NBT，格式与 paper 一致。
     */
    @Unique
    public byte[] serializeToBytes() throws IOException {
        CompoundTag root = new CompoundTag();
        root.tags.putAll(this.customDataTags);
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        NbtIo.writeCompressed(root, buf);
        return buf.toByteArray();
    }
}
