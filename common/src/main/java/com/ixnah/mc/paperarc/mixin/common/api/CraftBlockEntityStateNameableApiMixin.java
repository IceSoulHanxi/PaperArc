package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.craftbukkit.v.block.CraftBlockEntityState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code Nameable#customName()} 的方块侧实现体。
 *
 * <p>{@code org.bukkit.Nameable} 有**两族**实现类：实体（CraftEntity）与方块状态
 * （CraftContainer/CraftBeacon/CraftEnchantingTable…）。只给实体加实现体的话，
 * 插件对箱子之类调用就是 {@code AbstractMethodError}（B2-4 的 PARTIAL_IMPL 门禁抓到）。
 * 这里挂在公共基类 {@code CraftBlockEntityState} 上，一次覆盖全部方块状态。</p>
 */
@Mixin(CraftBlockEntityState.class)
public abstract class CraftBlockEntityStateNameableApiMixin {

    @Unique
    public Component customName() {
        String legacy = ((org.bukkit.Nameable) (Object) this).getCustomName();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void customName(Component customName) {
        ((org.bukkit.Nameable) (Object) this).setCustomName(customName == null ? null
                : LegacyComponentSerializer.legacySection().serialize(customName));
    }
}
