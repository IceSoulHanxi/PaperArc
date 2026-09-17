package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.generator.CraftWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * CraftWorldInfo 补齐 paper-api {@link org.bukkit.generator.WorldInfo} 新增的
 * {@code vanillaBiomeProvider()}（PERMANENT-BLOCKED，降级存根）。
 *
 * Paper 的实现依赖其新增的 6 参构造器把 vanillaChunkGenerator + RegistryAccess 存进
 * CraftWorldInfo 字段；Arclight 基础 jar 的 CraftWorldInfo 只有
 * (ServerLevelData, LevelStorageAccess, Environment, DimensionType) 与纯标量两个构造器，
 * 不持有任何生成器状态。尝试从构造参数推导也走不通：
 * 本 NMS 版本的 PrimaryLevelData 未公开 worldGenSettings/dimensions 访问器
 * （仅有 worldGenOptions()，拿不到 LevelStem.registry），DerivedLevelData 更无从下手。
 *
 * 因此降级抛 UnsupportedOperationException 并注明原因，避免返回错误的 Biome 数据。
 */
@Mixin(CraftWorldInfo.class)
public abstract class CraftWorldInfoApiMixin {

    @Unique
    public org.bukkit.generator.BiomeProvider vanillaBiomeProvider() {
        throw new UnsupportedOperationException(
            "PaperArc: CraftWorldInfo#vanillaBiomeProvider() needs Paper's extended constructor "
                + "storing vanillaChunkGenerator + RegistryAccess; Arclight's base CraftWorldInfo "
                + "holds neither and PrimaryLevelData exposes no dimension registry accessor");
    }

    /**
     * paper {@code FeatureFlagSetHolder#getFeatureFlags}（B3-2）。
     *
     * <p>{@code CraftWorldInfo} 只是世界生成阶段传给 ChunkGenerator 的信息快照，身上没有
     * {@code ServerLevel} 句柄，拿不到"这个世界"的开关；退一步取服务器存档级的
     * {@code WorldData#enabledFeatures()} —— vanilla 的特性开关本来就是整个存档一套，
     * 两者在实践中一致。
     */
    @Unique
    public java.util.Set<org.bukkit.FeatureFlag> getFeatureFlags() {
        net.minecraft.server.MinecraftServer server =
                ((org.bukkit.craftbukkit.v.CraftServer) com.ixnah.mc.paperarc.bridge.PaperArcBridge.getServer()).getServer();
        return java.util.Collections.unmodifiableSet(
                org.bukkit.craftbukkit.v.CraftFeatureFlag.getFromNMS(server.getWorldData().enabledFeatures()));
    }
}
