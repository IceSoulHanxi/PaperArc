package dev.paperarc.mixin.common.api;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.network.chat.Component.Serializer;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v.advancement.CraftAdvancement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's {@code Add-more-advancement-API} patch on
 * {@link CraftAdvancement}: {@code displayName()}, {@code getParent()},
 * {@code getChildren()} and {@code getRoot()}.
 */
@Mixin(CraftAdvancement.class)
public abstract class CraftAdvancementApiMixin {

    @Shadow
    public abstract AdvancementHolder getHandle();

    @Unique
    public Component displayName() {
        // PaperAdventure unavailable: gson round-trip instead of asAdventure (project convention)
        return GsonComponentSerializer.gson().deserialize(Serializer.toJson(
            net.minecraft.advancements.Advancement.name(this.getHandle()),
            ((org.bukkit.craftbukkit.v.CraftServer) dev.paperarc.bridge.PaperArcBridge.getServer()).getServer().registryAccess()));
    }

    @Unique
    public Advancement getParent() {
        AdvancementNode parent = this.paperarc$node();
        if (parent != null) {
            parent = parent.parent();
        }
        return parent == null ? null : new CraftAdvancement(parent.holder());
    }

    @Unique
    public Collection<Advancement> getChildren() {
        ImmutableList.Builder<Advancement> children = ImmutableList.builder();
        AdvancementNode node = this.paperarc$node();
        if (node != null) {
            for (AdvancementNode child : node.children()) {
                children.add(new CraftAdvancement(child.holder()));
            }
        }
        return children.build();
    }

    @Unique
    public Advancement getRoot() {
        AdvancementNode node = this.paperarc$node();
        if (node == null) {
            throw new IllegalStateException("could not find internal advancement node for advancement " + this.getHandle().id());
        }
        return new CraftAdvancement(node.root().holder());
    }

    @Unique
    private AdvancementNode paperarc$node() {
        return ((org.bukkit.craftbukkit.v.CraftServer) dev.paperarc.bridge.PaperArcBridge.getServer())
            .getServer().getAdvancements().tree().get(this.getHandle());
    }
}
