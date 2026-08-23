package dev.paperarc.mixin.common.api;

import dev.paperarc.bridge.PaperArcBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.entity.Display;
import org.bukkit.craftbukkit.v.entity.CraftTextDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Adventure text API on CraftTextDisplay.
 *
 * NMS accessors {@code Display.TextDisplay.getText/setText} are private in
 * vanilla 1.21.1 and are opened by paperarc.accesswidener (per project rule:
 * access problems are solved with the Access Widener, not reflection).
 * PaperAdventure is unavailable here, so a gson round-trip converts between
 * Adventure and vanilla components.
 */
@Mixin(CraftTextDisplay.class)
public abstract class CraftTextDisplayApiMixin {

    @Shadow
    public abstract Display.TextDisplay getHandle();

    @Unique
    public Component text() {
        net.minecraft.network.chat.Component vanilla = this.getHandle().getText();
        if (vanilla == null) {
            return Component.empty();
        }
        // PaperAdventure unavailable: gson round-trip instead
        String json = Serializer.toJson(vanilla,
                ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess());
        return GsonComponentSerializer.gson().deserialize(json);
    }

    @Unique
    public void text(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("text cannot be null");
        }
        String json = GsonComponentSerializer.gson().serialize(component);
        net.minecraft.network.chat.Component vanilla =
                Serializer.fromJson(json, ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess());
        this.getHandle().setText(vanilla);
    }
}
