package com.ixnah.mc.paperarc.bridge;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v.block.CraftBiome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.List;

/**
 * {@code WorldInfo#vanillaBiomeProvider()}（两个实现类共用；B8/Y-4 之前两处都抛 UOE）。
 *
 * <p>照 paper 的 Expose-vanilla-BiomeProvider-from-WorldInfo.patch：从
 * {@code ServerChunkCache} 取 vanilla 的 {@code BiomeSource} 与
 * {@code RandomState#sampler()}，包成一个只读 {@code BiomeProvider}。
 * Paper 是在 {@code CraftWorldInfo} 的扩展构造器里把这两样存下来，我们没有那个构造器，
 * 改成用时从 {@code ServerLevel} 现取 —— 取值一样。
 *
 * <p>1.21.1 与 1.20.1 的差别：Biome 的 NMS↔Bukkit 转换从
 * {@code CraftBlock.biomeBaseToBiome} 挪到了独立的 {@code CraftBiome}（`javap` 核对）。
 */
public final class PaperarcBiomeProviders {

    private PaperarcBiomeProviders() {
    }

    public static BiomeProvider fromLevel(ServerLevel level) {
        BiomeSource biomeSource = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        List<Biome> possibleBiomes = biomeSource.possibleBiomes().stream()
                .map(CraftBiome::minecraftHolderToBukkit)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        return new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                return CraftBiome.minecraftHolderToBukkit(
                        biomeSource.getNoiseBiome(x >> 2, y >> 2, z >> 2, sampler));
            }

            @Override
            public List<Biome> getBiomes(WorldInfo worldInfo) {
                return possibleBiomes;
            }
        };
    }

    /** {@code CraftWorldInfo} 身上没有 {@code ServerLevel} 句柄，按名字反查。 */
    public static ServerLevel levelOf(WorldInfo info) {
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(info.getUID());
        if (world == null) {
            world = org.bukkit.Bukkit.getWorld(info.getName());
        }
        if (world == null) {
            throw new IllegalStateException(
                    "vanillaBiomeProvider()：世界 " + info.getName() + " 还没加载完，拿不到 ServerLevel");
        }
        return ((org.bukkit.craftbukkit.v.CraftWorld) world).getHandle();
    }
}
