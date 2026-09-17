package com.ixnah.mc.paperarc.mixin.common.api;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.craftbukkit.v.inventory.CraftMetaBook;
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
// CraftMetaBookSigned 直接继承 CraftMetaItem 并实现 BookMeta（不继承 CraftMetaBook），
// 不一起挂的话成书调 title()/author()/toBuilder() 就是 AbstractMethodError（PARTIAL_IMPL 门禁）。
// 多目标 mixin 的硬要求：每个 @Shadow 都要写 remap = false —— 一个 @Shadow 在多个目标之间
// 没法共用一条 refmap 条目，Mixin 直接 InvalidMixinException
// （实测 "Found a remappable @Shadow annotation on getTitle"，整个 mixin 不应用，
// 连原本单目标时能用的方法也一起没了）。注意写在 @Mixin 上的 remap = false **不管用**，
// 它只影响注解处理器生成 refmap，运行期查的是 @Shadow 自己的 remap。
// 这里目标全是 CraftBukkit 类，本来就不参与重映射。
@Mixin(value = {CraftMetaBook.class, org.bukkit.craftbukkit.v.inventory.CraftMetaBookSigned.class},
       remap = false)
public abstract class CraftMetaBookApiMixin {

    @Shadow(remap = false)
    public abstract String getTitle();

    @Shadow(remap = false)
    public abstract boolean setTitle(String title);

    @Shadow(remap = false)
    public abstract String getAuthor();

    @Shadow(remap = false)
    public abstract void setAuthor(String author);

    @Shadow(remap = false)
    public abstract String getPage(int index);

    @Shadow(remap = false)
    public abstract void setPage(int index, String page);

    @Shadow(remap = false)
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
        // 不要写成 mixin 里的匿名类：Mixin 会把它搬进 CraftMetaBook，搬完的
        // InnerClasses/NestHost 仍指向本 mixin → IncompatibleClassChangeError（§1.6 c）。
        return new com.ixnah.mc.paperarc.bridge.PaperarcBookMetaBuilder((BookMeta) (Object) this);
    }
}
