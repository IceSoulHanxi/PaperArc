package com.ixnah.mc.paperarc.bridge;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.List;

/**
 * {@code WorldInfo#vanillaBiomeProvider()} / {@code World#vanillaBiomeProvider()} 的公共实现
 * （A8/Y-4；两处原先都抛 UOE）。
 *
 * <p>照抄 paper 的 Expose-vanilla-BiomeProvider-from-WorldInfo.patch：
 * 从 {@code ServerChunkCache} 取 vanilla 的 {@code BiomeSource} 与
 * {@code RandomState#sampler()}，包成一个只读的 {@code BiomeProvider}。
 */
public final class PaperarcBiomeProviders {

    private PaperarcBiomeProviders() {
    }

    public static BiomeProvider fromLevel(ServerLevel level) {
        BiomeSource biomeSource = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        Registry<net.minecraft.world.level.biome.Biome> biomeRegistry =
                level.registryAccess().registryOrThrow(Registries.BIOME);
        List<Biome> possibleBiomes = biomeSource.possibleBiomes().stream()
                .map(biome -> CraftBlock.biomeBaseToBiome(biomeRegistry, biome))
                .toList();
        return new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                return CraftBlock.biomeBaseToBiome(biomeRegistry,
                        biomeSource.getNoiseBiome(x >> 2, y >> 2, z >> 2, sampler));
            }

            @Override
            public List<Biome> getBiomes(WorldInfo worldInfo) {
                return possibleBiomes;
            }
        };
    }
}
