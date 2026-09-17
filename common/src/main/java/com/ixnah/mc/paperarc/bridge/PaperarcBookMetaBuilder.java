package com.ixnah.mc.paperarc.bridge;

import java.util.Collection;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.BookMeta;

/**
 * {@code BookMeta#toBuilder()} 返回的 builder：就地修改宿主 meta（Arclight 没有
 * builder 管线），只为满足注入的 {@code BookMeta.BookMetaBuilder} 接口。
 *
 * <p>放在 {@code bridge} 而不是写成 mixin 里的匿名类：Mixin 会把 mixin 的匿名类搬进目标类，
 * 搬完的 InnerClasses/NestHost 仍指向原 mixin 外围类 → {@code IncompatibleClassChangeError}
 * （checklist §1.6 c）。全部调用都走 {@code BookMeta} 接口上由 IfaceMixin 声明的方法，
 * 不碰 CraftMetaBook 的私有成员。</p>
 *
 * <p>{@code BookMeta.BookMetaBuilder} 在 1.20.1 运行时缺失，由 RuntimeClassInjector 注入
 * （见 {@code META-INF/paperarc/runtime/injections.json}）；本类只在 {@code toBuilder()}
 * 被调用时才加载，那时注入早已完成。</p>
 */
public final class PaperarcBookMetaBuilder implements BookMeta.BookMetaBuilder {

    private final BookMeta meta;

    public PaperarcBookMetaBuilder(BookMeta meta) {
        this.meta = meta;
    }

    @Override
    public BookMeta.BookMetaBuilder title(Component component) {
        this.meta.title(component);
        return this;
    }

    @Override
    public BookMeta.BookMetaBuilder author(Component component) {
        this.meta.author(component);
        return this;
    }

    @Override
    public BookMeta.BookMetaBuilder addPage(Component component) {
        this.meta.addPages(component);
        return this;
    }

    @Override
    public BookMeta.BookMetaBuilder pages(Component... components) {
        this.meta.addPages(components);
        return this;
    }

    @Override
    public BookMeta.BookMetaBuilder pages(Collection<Component> components) {
        this.meta.addPages(components.toArray(new Component[0]));
        return this;
    }

    @Override
    public BookMeta build() {
        return this.meta;
    }
}
