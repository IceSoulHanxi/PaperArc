package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcBiomeProviders;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.bukkit.World;
import org.bukkit.craftbukkit.v.generator.CraftWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CraftWorldInfo 补齐 paper-api {@link org.bukkit.generator.WorldInfo} 新增的
 * {@code vanillaBiomeProvider()}。
 *
 * <p>Paper 的实现靠它自己加的 6 参构造器把 {@code vanillaChunkGenerator + RegistryAccess}
 * 存进字段；Arclight 的 {@code CraftWorldInfo} 只有 4 参构造器，两样都没有。
 *
 * <p>A8/Y-4 改成真实现：**唯一**的构造点在 Arclight 的 {@code Level#getWorld()} 里
 * （`grep` 核对，只有那一处 {@code new CraftWorldInfo(...)}），
 * 那时对应的 {@code ServerLevel} 已经存在、只是 {@code CraftWorld} 还没造出来。
 * 于是在构造器里记下传进来的 {@code ServerLevelData}，
 * 用它在 {@code server.getAllLevels()} 里按**对象同一性**反查 {@code ServerLevel}
 * （不是按名字：世界名可能重复/尚未注册），再走与 {@code CraftWorld} 同一套公式。
 */
@Mixin(CraftWorldInfo.class)
public abstract class CraftWorldInfoApiMixin {

    @Unique
    private ServerLevelData paperarc$levelData;

    @Inject(method = "<init>(Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lorg/bukkit/World$Environment;Lnet/minecraft/world/level/dimension/DimensionType;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureLevelData(ServerLevelData worldDataServer,
                                           LevelStorageSource.LevelStorageAccess session,
                                           World.Environment environment,
                                           net.minecraft.world.level.dimension.DimensionType dimensionManager,
                                           CallbackInfo ci) {
        this.paperarc$levelData = worldDataServer;
    }

    @Unique
    public org.bukkit.generator.BiomeProvider vanillaBiomeProvider() {
        ServerLevelData data = this.paperarc$levelData;
        if (data != null) {
            net.minecraft.server.MinecraftServer server =
                    ((org.bukkit.craftbukkit.v.CraftServer) org.bukkit.Bukkit.getServer()).getServer();
            for (ServerLevel level : server.getAllLevels()) {
                if (level.getLevelData() == data) {
                    return PaperarcBiomeProviders.fromLevel(level);
                }
            }
        }
        throw new UnsupportedOperationException(
                "PaperArc: CraftWorldInfo#vanillaBiomeProvider() 找不到对应的 ServerLevel"
                        + "（这个 WorldInfo 不是由 Arclight 的 Level#getWorld() 造出来的）");
    }
}
