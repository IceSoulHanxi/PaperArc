package dev.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.craftbukkit.v.block.CraftCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's {@code More-CommandBlock-API}/{@code Adventure} additions on
 * {@link CraftCommandBlock}: adventure {@code name()} / {@code name(Component)}
 * (PaperAdventure unavailable: gson round-trip, project convention), plus
 * {@code isConditional()}/{@code setConditional(boolean)} which Paper keeps on
 * the block-data type — delegated to the snapshot's data impl here.
 */
@Mixin(CraftCommandBlock.class)
public abstract class CraftCommandBlockApiMixin {

    @Shadow
    protected abstract CommandBlockEntity getSnapshot();

    @Shadow
    public abstract BlockData getBlockData();

    @Shadow
    public abstract void setBlockData(BlockData blockData);

    @Unique
    public Component name() {
        net.minecraft.core.HolderLookup.Provider lookup = ((org.bukkit.craftbukkit.v.CraftServer) dev.paperarc.bridge.PaperArcBridge.getServer())
            .getServer().registryAccess();
        return GsonComponentSerializer.gson().deserialize(Serializer.toJson(this.getSnapshot().getCommandBlock().getName(), lookup));
    }

    @Unique
    public void name(Component name) {
        // As in Paper: a null component resets to the vanilla "@" fallback name
        net.minecraft.network.chat.Component vanilla = name == null
            ? net.minecraft.network.chat.Component.literal("@")
            : Serializer.fromJson(GsonComponentSerializer.gson().serialize(name),
                ((org.bukkit.craftbukkit.v.CraftServer) dev.paperarc.bridge.PaperArcBridge.getServer()).getServer().registryAccess());
        this.getSnapshot().getCommandBlock().setCustomName(vanilla);
    }

    @Unique
    public boolean isConditional() {
        return ((CommandBlock) this.getBlockData()).isConditional();
    }

    @Unique
    public void setConditional(boolean conditional) {
        CommandBlock data = (CommandBlock) this.getBlockData();
        data.setConditional(conditional);
        this.setBlockData(data);
    }
}
