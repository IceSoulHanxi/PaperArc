package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.Collection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMetaBook;
import org.bukkit.inventory.meta.BookMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's adventure {@code Component} variants of the book meta API to
 * {@link CraftMetaBook}: {@code author()}, {@code title()}, {@code page(int)},
 * their setters, {@code addPages(Component...)}, and {@code toBuilder()}.
 *
 * <p>Conversion mirrors Paper ({@code PaperAdventure.LEGACY_SECTION_UX}). The
 * {@code BookMetaBuilder} returned by {@code toBuilder()} mutates this meta in
 * place (Arclight has no builder plumbing) but satisfies the injected interface.</p>
 */
@Mixin(CraftMetaBook.class)
public abstract class CraftMetaBookApiMixin {

    @Shadow
    public abstract String getTitle();

    @Shadow
    public abstract boolean setTitle(String title);

    @Shadow
    public abstract String getAuthor();

    @Shadow
    public abstract void setAuthor(String author);

    @Shadow
    public abstract String getPage(int index);

    @Shadow
    public abstract void setPage(int index, String page);

    @Shadow
    public abstract void addPage(String... pages);

    @Unique
    public Component author() {
        return getAuthor() == null ? null
                : LegacyComponentSerializer.legacySection().deserialize(getAuthor());
    }

    @Unique
    public Component title() {
        return getTitle() == null ? null
                : LegacyComponentSerializer.legacySection().deserialize(getTitle());
    }

    @Unique
    public Component page(int index) {
        return LegacyComponentSerializer.legacySection().deserialize(getPage(index));
    }

    @Unique
    public BookMeta author(Component component) {
        setAuthor(component == null ? null
                : LegacyComponentSerializer.legacySection().serialize(component));
        return (BookMeta) (Object) this;
    }

    @Unique
    public BookMeta title(Component component) {
        setTitle(component == null ? null
                : LegacyComponentSerializer.legacySection().serialize(component));
        return (BookMeta) (Object) this;
    }

    @Unique
    public void page(int index, Component component) {
        setPage(index, component == null ? null
                : LegacyComponentSerializer.legacySection().serialize(component));
    }

    @Unique
    public void addPages(Component... pages) {
        if (pages == null) {
            return;
        }
        for (Component page : pages) {
            if (page != null) {
                addPage(LegacyComponentSerializer.legacySection().serialize(page));
            }
        }
    }

    @Unique
    public BookMeta.BookMetaBuilder toBuilder() {
        return new BookMeta.BookMetaBuilder() {
            @Override
            public BookMeta.BookMetaBuilder title(Component component) {
                CraftMetaBookApiMixin.this.title(component);
                return this;
            }

            @Override
            public BookMeta.BookMetaBuilder author(Component component) {
                CraftMetaBookApiMixin.this.author(component);
                return this;
            }

            @Override
            public BookMeta.BookMetaBuilder addPage(Component component) {
                CraftMetaBookApiMixin.this.addPages(component);
                return this;
            }

            @Override
            public BookMeta.BookMetaBuilder pages(Component... components) {
                CraftMetaBookApiMixin.this.addPages(components);
                return this;
            }

            @Override
            public BookMeta.BookMetaBuilder pages(Collection<Component> components) {
                CraftMetaBookApiMixin.this.addPages(components.toArray(new Component[0]));
                return this;
            }

            @Override
            public BookMeta build() {
                return (BookMeta) (Object) CraftMetaBookApiMixin.this;
            }
        };
    }
}
