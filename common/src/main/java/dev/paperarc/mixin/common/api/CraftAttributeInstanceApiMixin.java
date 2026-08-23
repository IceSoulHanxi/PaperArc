package dev.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v.attribute.CraftAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's attribute modifier lookup/removal API (Attribute-Modifier-API-improvements).
 *
 * Key-based lookups map adventure {@link net.kyori.adventure.key.Key} to
 * {@link ResourceLocation} directly (PaperAdventure.asVanilla equivalent).
 * UUID-based lookups replace Paper's legacy AttributeMappings table with a scan of
 * the live modifiers, comparing the UUID CraftBukkit derives for each — covers every
 * registered modifier rather than only Paper's hardcoded well-known UUIDs.
 */
@Mixin(CraftAttributeInstance.class)
public abstract class CraftAttributeInstanceApiMixin {

    @Shadow
    private AttributeInstance handle;

    @Unique
    private static ResourceLocation paperarc$asVanilla(net.kyori.adventure.key.Key key) {
        return ResourceLocation.fromNamespaceAndPath(key.namespace(), key.value());
    }

    @Unique
    public void addTransientModifier(AttributeModifier modifier) {
        Preconditions.checkArgument(modifier != null, "modifier");
        this.handle.addTransientModifier(CraftAttributeInstance.convert(modifier));
    }

    @Unique
    public AttributeModifier getModifier(java.util.UUID uuid) {
        Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        for (net.minecraft.world.entity.ai.attributes.AttributeModifier nms : this.handle.getModifiers()) {
            AttributeModifier bukkit = CraftAttributeInstance.convert(nms);
            if (uuid.equals(bukkit.getUniqueId())) {
                return bukkit;
            }
        }
        return null;
    }

    @Unique
    public AttributeModifier getModifier(net.kyori.adventure.key.Key key) {
        Preconditions.checkArgument(key != null, "Key cannot be null");
        net.minecraft.world.entity.ai.attributes.AttributeModifier nms = this.handle.getModifier(paperarc$asVanilla(key));
        return nms == null ? null : CraftAttributeInstance.convert(nms);
    }

    @Unique
    public void removeModifier(java.util.UUID uuid) {
        Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        for (net.minecraft.world.entity.ai.attributes.AttributeModifier nms : this.handle.getModifiers()) {
            if (uuid.equals(CraftAttributeInstance.convert(nms).getUniqueId())) {
                this.handle.removeModifier(nms.id());
                return;
            }
        }
    }

    @Unique
    public void removeModifier(net.kyori.adventure.key.Key key) {
        Preconditions.checkArgument(key != null, "Key cannot be null");
        this.handle.removeModifier(paperarc$asVanilla(key));
    }
}
