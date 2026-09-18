package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code ChunkGenerator#createVanillaChunkData(World,int,int)}（A6/X-2 第五批）：
 * 就是转发到 {@code Bukkit.createVanillaChunkData}（Server 侧本仓库已补）。
 */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorApiMixin {

    @Unique
    @SuppressWarnings("removal")
    public ChunkGenerator.ChunkData createVanillaChunkData(World world, int x, int z) {
        return Bukkit.getServer().createVanillaChunkData(world, x, z);
    }
}
