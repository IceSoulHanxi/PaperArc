package com.ixnah.mc.paperarc.mixin.common.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
 *
 * <p><b>A5-2（checklist §1.9 ah 核对结论）</b>：main 在 1.21.1 上把 {@code readFromBytes}
 * 改成了 {@code NbtIo.readCompressed}，1.20.1 <b>不能照搬</b> —— Paper 1.20.1 的
 * {@code Added-byte-array-serialization-deserialization-for-P.patch} 两侧写的都是
 * <b>不压缩</b>的 {@code NbtIo.write}/{@code NbtIo.read}（gzip 是后续版本才换的），
 * 照搬会与 Paper 1.20.1 的字节格式不兼容。真正缺的是**另一半**：运行时与本仓库都没有
 * {@code serializeToBytes()}，插件一调就是 {@code AbstractMethodError}，这里补上。
 */
@Mixin(CraftPersistentDataContainer.class)
public abstract class CraftPersistentDataContainerApiMixin {

    @Shadow
    private Map<String, Tag> customDataTags;

    @Shadow
    public abstract void putAll(Map<String, Tag> map);

    @Shadow
    public abstract CompoundTag toTagCompound();

    /** paper {@code PersistentDataContainer#serializeToBytes}，格式与 Paper 1.20.1 一致（不压缩）。 */
    @Unique
    public byte[] serializeToBytes() throws IOException {
        CompoundTag root = this.toTagCompound();
        ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
        try (DataOutputStream dataOutput = new DataOutputStream(byteArrayOutput)) {
            NbtIo.write(root, dataOutput);
            return byteArrayOutput.toByteArray();
        }
    }

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
