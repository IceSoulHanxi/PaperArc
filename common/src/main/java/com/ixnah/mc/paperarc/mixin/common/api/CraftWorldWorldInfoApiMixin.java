package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code WorldInfo#vanillaBiomeProvider()} 的第二个实现类：{@code CraftWorld} 也实现
 * {@code WorldInfo}，与 {@code CraftWorldInfo} 互不继承（PARTIAL_IMPL 门禁抓到）。
 * 降级理由与 {@link CraftWorldInfoApiMixin} 完全相同。
 */
@Mixin(CraftWorld.class)
public abstract class CraftWorldWorldInfoApiMixin {

    @Unique
    public org.bukkit.generator.BiomeProvider vanillaBiomeProvider() {
        throw new UnsupportedOperationException(
            "PaperArc: vanillaBiomeProvider() needs Paper's extended CraftWorldInfo constructor "
                + "storing vanillaChunkGenerator + RegistryAccess; Arclight holds neither");
    }
}
