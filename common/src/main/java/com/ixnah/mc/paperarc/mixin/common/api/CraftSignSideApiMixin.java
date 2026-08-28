package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import org.bukkit.craftbukkit.v.block.sign.CraftSignSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Port of Paper's Adventure additions on {@link CraftSignSide}:
 * {@code lines()}, {@code line(int)} and {@code line(int, Component)}.
 *
 * PaperAdventure is unavailable here, so vanilla ↔ adventure conversion uses
 * the gson round-trip (as in CraftTextDisplayApiMixin). Paper defers writing
 * components back into the NMS {@code SignText} until
 * {@code applyLegacyStringToSignSide()}; since this codebase keeps the
 * String-based legacy path, we write through to {@code SignText#setMessage}
 * immediately instead (semantics preserved, no lost edits). The private
 * {@code signText} field is read/written reflectively.
 */
@Mixin(CraftSignSide.class)
public abstract class CraftSignSideApiMixin {

    @Unique
    private static volatile Field PAPERARC$SIGN_TEXT_FIELD;

    @Unique
    private ArrayList<Component> paperarc$lines;

    @Unique
    public List<Component> lines() {
        this.paperarc$loadLines();
        return this.paperarc$lines;
    }

    @Unique
    public Component line(int index) {
        this.paperarc$loadLines();
        return this.paperarc$lines.get(index);
    }

    @Unique
    public void line(int index, Component line) {
        Preconditions.checkArgument(line != null, "Line cannot be null");
        this.paperarc$loadLines();
        this.paperarc$lines.set(index, line); // throws IndexOutOfBoundsException like Paper
        // Write through to the NMS sign text immediately (see class javadoc)
        try {
            net.minecraft.world.level.block.entity.SignText signText = this.paperarc$getSignText();
            this.paperarc$setSignText(signText.setMessage(index, paperarc$toVanilla(line)));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS SignText field not accessible on CraftSignSide", e);
        }
    }

    @Unique
    private void paperarc$loadLines() {
        if (this.paperarc$lines != null) {
            return;
        }
        ArrayList<Component> loaded = new ArrayList<>(4);
        try {
            for (net.minecraft.network.chat.Component message : this.paperarc$getSignText().getMessages(false)) {
                loaded.add(paperarc$fromVanilla(message));
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS SignText field not accessible on CraftSignSide", e);
        }
        this.paperarc$lines = loaded;
    }

    @Unique
    private net.minecraft.world.level.block.entity.SignText paperarc$getSignText()
            throws ReflectiveOperationException {
        return (net.minecraft.world.level.block.entity.SignText) paperarc$signTextField().get((Object) this);
    }

    @Unique
    private void paperarc$setSignText(net.minecraft.world.level.block.entity.SignText signText)
            throws ReflectiveOperationException {
        paperarc$signTextField().set((Object) this, signText);
    }

    @Unique
    private static Field paperarc$signTextField() throws NoSuchFieldException {
        Field field = PAPERARC$SIGN_TEXT_FIELD;
        if (field == null) {
            synchronized (CraftSignSideApiMixin.class) {
                if (PAPERARC$SIGN_TEXT_FIELD == null) {
                    Field declared = CraftSignSide.class.getDeclaredField("signText");
                    declared.setAccessible(true);
                    PAPERARC$SIGN_TEXT_FIELD = declared;
                }
                field = PAPERARC$SIGN_TEXT_FIELD;
            }
        }
        return field;
    }

    @Unique
    private static Component paperarc$fromVanilla(net.minecraft.network.chat.Component vanilla) {
        String json = Serializer.toJson(vanilla);
        return GsonComponentSerializer.gson().deserialize(json);
    }

    @Unique
    private static net.minecraft.network.chat.Component paperarc$toVanilla(Component component) {
        String json = GsonComponentSerializer.gson().serialize(component);
        return Serializer.fromJson(json);
    }
}
