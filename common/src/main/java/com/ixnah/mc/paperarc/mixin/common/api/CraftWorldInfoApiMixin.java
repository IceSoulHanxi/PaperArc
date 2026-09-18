package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.generator.CraftWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * CraftWorldInfo 补齐 paper-api {@link org.bukkit.generator.WorldInfo} 新增的
 * {@code vanillaBiomeProvider()} 与 {@code getFeatureFlags()}。
 *
 * <p>B8/Y-4：{@code vanillaBiomeProvider()} 从"降级抛 UOE"换成真实现。Paper 是在它新增的
 * 6 参构造器里把 vanillaChunkGenerator + RegistryAccess 存进 CraftWorldInfo 字段，
 * Arclight 的基础 jar 没有那个构造器；改成**按世界名/UID 反查 {@code ServerLevel}**
 * 再从 {@code ServerChunkCache} 现取 BiomeSource 与 sampler，取值与 Paper 一致
 * （见 {@link com.ixnah.mc.paperarc.bridge.PaperarcBiomeProviders}）。
 * 唯一的限制：世界还在生成、尚未注册进 Bukkit 时抛 IllegalStateException，
 * 而不是返回一个错的 provider。
 */
@Mixin(CraftWorldInfo.class)
public abstract class CraftWorldInfoApiMixin {

    @Unique
    public org.bukkit.generator.BiomeProvider vanillaBiomeProvider() {
        return com.ixnah.mc.paperarc.bridge.PaperarcBiomeProviders.fromLevel(
                com.ixnah.mc.paperarc.bridge.PaperarcBiomeProviders.levelOf(
                        (org.bukkit.generator.WorldInfo) (Object) this));
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
