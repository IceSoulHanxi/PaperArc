package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Paper 的 {@code ServerLinks.ServerLink#displayName()}（adventure 版的 getDisplayName）。 */
@Mixin(targets = "org.bukkit.craftbukkit.v.CraftServerLinks$CraftServerLink")
public abstract class CraftServerLinkApiMixin {

    @Unique
    public Component displayName() {
        String legacy = ((org.bukkit.ServerLinks.ServerLink) (Object) this).getDisplayName();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }
}
