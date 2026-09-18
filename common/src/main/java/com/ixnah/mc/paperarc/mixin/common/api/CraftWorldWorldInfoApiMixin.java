package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code WorldInfo#vanillaBiomeProvider()} 的第二个实现类：{@code CraftWorld} 也实现
 * {@code WorldInfo}，与 {@code CraftWorldInfo} 互不继承（PARTIAL_IMPL 门禁抓到）。
 *
 * <p>B8/Y-4：这一侧比 {@code CraftWorldInfo} 还直接 —— {@code getHandle()} 就是
 * {@code ServerLevel}，不用反查。
 */
@Mixin(CraftWorld.class)
public abstract class CraftWorldWorldInfoApiMixin {

    @Shadow
    public abstract net.minecraft.server.level.ServerLevel getHandle();

    @Unique
    public org.bukkit.generator.BiomeProvider vanillaBiomeProvider() {
        return com.ixnah.mc.paperarc.bridge.PaperarcBiomeProviders.fromLevel(this.getHandle());
    }
}
