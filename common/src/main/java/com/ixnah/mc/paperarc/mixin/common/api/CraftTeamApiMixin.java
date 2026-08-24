package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import org.bukkit.craftbukkit.v.scoreboard.CraftScoreboard;
import org.bukkit.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Adventure/entity-membership API to CraftTeam.
 *
 * Paper refs: Adventure.patch, Improve-scoreboard-entries.patch,
 * Multiple-Entries-with-Scoreboards.patch.
 *
 * NMS PlayerTeam (mojmap 1.21.1, verified via javap): getName(), getDisplayName()/
 * setDisplayName(Component), getPlayerPrefix()/setPlayerPrefix(Component),
 * getPlayerSuffix()/setPlayerSuffix(Component), getColor()/setColor(ChatFormatting).
 * ChatFormatting: RESET constant, getColor() -> Integer (null for RESET), getByName(String).
 *
 * Membership operations reuse CraftTeam's own public addEntry/removeEntry/hasEntry
 * (which already perform the unregistered-state check), keyed by entity UUID string,
 * mirroring Paper's Improve-scoreboard-entries implementations. Component conversion
 * uses gson round-trips because Paper's io.papermc.paper.adventure.PaperAdventure is
 * server-only.
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.scoreboard.CraftTeam")
public abstract class CraftTeamApiMixin {

    @Shadow
    @Final
    private net.minecraft.world.scores.PlayerTeam team;

    @Shadow
    public abstract void addEntry(String entry);

    @Shadow
    public abstract boolean removeEntry(String entry);

    @Shadow
    public abstract boolean hasEntry(String entry);

    /** Package-private in CraftTeam; shadowed so direct-NMS setters keep its IllegalStateException contract. */
    @Shadow
    abstract CraftScoreboard checkState();

    // ------------------------------------------------------------------ API

    @Unique
    public void addEntity(Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");
        this.checkState();
        this.addEntry(entity.getUniqueId().toString());
    }

    @Unique
    public void addEntities(java.util.Collection<Entity> entities) {
        Preconditions.checkArgument(entities != null && !entities.isEmpty(), "Entities cannot be empty");
        for (Entity entity : entities) {
            this.addEntity(entity);
        }
    }

    @Unique
    public void addEntries(java.util.Collection<String> entries) {
        Preconditions.checkArgument(entries != null && !entries.isEmpty(), "Entries cannot be empty");
        for (String entry : entries) {
            this.addEntry(entry);
        }
    }

    @Unique
    public TextColor color() {
        this.checkState();
        net.minecraft.ChatFormatting formatting = this.team.getColor();
        return formatting == net.minecraft.ChatFormatting.RESET || formatting.getColor() == null
                ? null : TextColor.color(formatting.getColor());
    }

    @Unique
    public void color(NamedTextColor color) {
        this.checkState();
        this.team.setColor(color == null ? net.minecraft.ChatFormatting.RESET
                : java.util.Objects.requireNonNull(net.minecraft.ChatFormatting.getByName(color.toString()),
                        () -> "Unknown NamedTextColor: " + color));
    }

    @Unique
    public boolean hasColor() {
        return this.team.getColor().getColor() != null;
    }

    @Unique
    public Component displayName() {
        this.checkState();
        return paperarc$asAdventure(this.team.getDisplayName());
    }

    @Unique
    public void displayName(Component displayName) {
        this.checkState();
        this.team.setDisplayName(displayName == null
                ? net.minecraft.network.chat.Component.empty()
                : paperarc$asVanilla(displayName));
    }

    @Unique
    public Component prefix() {
        this.checkState();
        return paperarc$asAdventure(this.team.getPlayerPrefix());
    }

    @Unique
    public void prefix(Component prefix) {
        this.checkState();
        this.team.setPlayerPrefix(prefix == null
                ? net.minecraft.network.chat.Component.empty()
                : paperarc$asVanilla(prefix));
    }

    @Unique
    public Component suffix() {
        this.checkState();
        return paperarc$asAdventure(this.team.getPlayerSuffix());
    }

    @Unique
    public void suffix(Component suffix) {
        this.checkState();
        this.team.setPlayerSuffix(suffix == null
                ? net.minecraft.network.chat.Component.empty()
                : paperarc$asVanilla(suffix));
    }

    @Unique
    public boolean hasEntity(Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");
        return this.hasEntry(entity.getUniqueId().toString());
    }

    @Unique
    public boolean removeEntity(Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");
        return this.removeEntry(entity.getUniqueId().toString());
    }

    @Unique
    public boolean removeEntities(java.util.Collection<Entity> entities) {
        Preconditions.checkArgument(entities != null && !entities.isEmpty(), "Entities cannot be empty");
        boolean successfullyRemoved = true;
        for (Entity entity : entities) {
            if (!this.removeEntity(entity)) {
                successfullyRemoved = false;
            }
        }
        return successfullyRemoved;
    }

    @Unique
    public boolean removeEntries(java.util.Collection<String> entries) {
        Preconditions.checkArgument(entries != null && !entries.isEmpty(), "Entries cannot be empty");
        boolean successfullyRemoved = true;
        for (String entry : entries) {
            if (!this.removeEntry(entry)) {
                successfullyRemoved = false;
            }
        }
        return successfullyRemoved;
    }

    // -------------------------------------------------------------- helpers

    @Unique
    private static Component paperarc$asAdventure(net.minecraft.network.chat.Component vanilla) {
        return GsonComponentSerializer.gson()
                .deserialize(Serializer.toJson(vanilla, paperarc$nmsServer().registryAccess()));
    }

    @Unique
    private static net.minecraft.network.chat.Component paperarc$asVanilla(Component adventure) {
        return Serializer.fromJson(GsonComponentSerializer.gson().serialize(adventure),
                paperarc$nmsServer().registryAccess());
    }

    /** NMS MinecraftServer via CraftBukkit's server accessor (for component serialization). */
    @Unique
    private static net.minecraft.server.MinecraftServer paperarc$nmsServer() {
        return ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer();
    }
}
