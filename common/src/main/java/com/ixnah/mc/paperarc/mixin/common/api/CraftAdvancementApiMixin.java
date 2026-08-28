package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_20_R1.advancement.CraftAdvancement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's {@code Add-more-advancement-API} patch on
 * {@link CraftAdvancement}: {@code displayName()}, {@code getParent()},
 * {@code getChildren()} and {@code getRoot()}.
 *
 * 1.20.1 uses the old {@link net.minecraft.advancements.Advancement} class
 * (no Holder/Node/Tree), and Paper's {@code constructDisplayComponent} helper
 * is a paper-server-only addition, so displayName is reimplemented from
 * {@code DisplayInfo#getTitle()} here.
 */
@Mixin(CraftAdvancement.class)
public abstract class CraftAdvancementApiMixin {

    @Shadow
    public abstract net.minecraft.advancements.Advancement getHandle();

    @Unique
    public Component displayName() {
        net.minecraft.advancements.Advancement handle = this.getHandle();
        net.minecraft.network.chat.Component title;
        if (handle.getDisplay() == null) {
            title = net.minecraft.network.chat.Component.literal(handle.getId().toString());
        } else {
            title = handle.getDisplay().getTitle();
        }
        return GsonComponentSerializer.gson().deserialize(Serializer.toJson(title));
    }

    @Unique
    public Advancement getParent() {
        net.minecraft.advancements.Advancement parent = this.getHandle().getParent();
        return parent == null ? null : new CraftAdvancement(parent);
    }

    @Unique
    public Collection<Advancement> getChildren() {
        ImmutableList.Builder<Advancement> children = ImmutableList.builder();
        for (net.minecraft.advancements.Advancement child : this.getHandle().getChildren()) {
            children.add(new CraftAdvancement(child));
        }
        return children.build();
    }

    @Unique
    public Advancement getRoot() {
        net.minecraft.advancements.Advancement advancement = this.getHandle();
        while (advancement.getParent() != null) {
            advancement = advancement.getParent();
        }
        return new CraftAdvancement(advancement);
    }
}
