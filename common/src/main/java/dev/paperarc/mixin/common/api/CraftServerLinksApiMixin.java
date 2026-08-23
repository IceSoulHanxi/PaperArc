package dev.paperarc.mixin.common.api;

import java.net.URI;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v.CraftServerLinks;
import org.bukkit.craftbukkit.v.CraftServerLinks.CraftServerLink;
import org.bukkit.ServerLinks.ServerLink;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.server.ServerLinks.Entry;

import dev.paperarc.bridge.PaperArcBridge;

/**
 * Adds Adventure addLink overload missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Adventure.patch (CraftServerLinks#addLink(Component, URI)).
 */
@Mixin(CraftServerLinks.class)
public abstract class CraftServerLinksApiMixin {

    @Shadow
    private void addLink(CraftServerLink link) {
        throw new AssertionError("shadow");
    }

    @Unique
    public ServerLink addLink(Component displayName, URI url) {
        Preconditions.checkArgument(displayName != null, "displayName cannot be null");
        Preconditions.checkArgument(url != null, "url cannot be null");

        // PaperAdventure unavailable: gson round-trip instead
        String json = GsonComponentSerializer.gson().serialize(displayName);
        net.minecraft.network.chat.Component vanilla =
                Serializer.fromJson(json, ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess());
        Entry entry = Entry.custom(vanilla, url);
        CraftServerLink link = new CraftServerLink(entry);
        this.addLink(link);
        return link;
    }
}
