package dev.paperarc.mixin.common.api;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v.tag.CraftTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's generic {@link org.bukkit.Tag} membership API on CraftTag.
 *
 * Membership is resolved purely via registry locations so it works for every
 * registry without per-type converters (Arclight has no CraftNamespacedKey#toMinecraft).
 */
@Mixin(CraftTag.class)
public abstract class CraftTagApiMixin {

    @Shadow
    public abstract net.minecraft.core.HolderSet.Named<?> getHandle();

    @Unique
    private static net.minecraft.resources.ResourceLocation paperarc$toResourceLocation(NamespacedKey key) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());
    }

    @Unique
    public boolean isTagged(Keyed item) {
        net.minecraft.resources.ResourceLocation location = paperarc$toResourceLocation(item.getKey());
        for (net.minecraft.core.Holder<?> holder : getHandle()) {
            java.util.Optional<? extends net.minecraft.resources.ResourceKey<?>> key = holder.unwrapKey();
            if (key.isPresent() && key.get().location().equals(location)) {
                return true;
            }
        }
        return false;
    }
}
