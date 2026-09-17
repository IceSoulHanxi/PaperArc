package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.craftbukkit.v.inventory.CraftMetaBook;
import org.bukkit.inventory.meta.BookMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * paper 的 {@code BookMeta extends net.kyori.adventure.inventory.Book}（B3-2）。
 *
 * <p><b>为什么要单独开一个 mixin 类</b>：{@code Book} 声明的是
 * {@code Book title(Component)} / {@code Book author(Component)}，而 paper 的
 * {@code BookMeta} 把它们协变收窄成返回 {@code BookMeta}（paper-api 里那两条是 default
 * 桥方法）。JVM 按**完整描述符**解析接口方法，只有返回 {@code BookMeta} 的那份等于没实现
 * （插件按 {@code Book} 调用即 {@code AbstractMethodError}）。java 不允许同一个类里出现
 * 只有返回类型不同的两个方法，所以返回 {@code Book} 的桥只能写在另一个 mixin 类里 ——
 * 合并进目标后就是两个描述符不同的同名方法，字节码层面完全合法。
 *
 * <p>{@code CraftMetaBookSigned} 与 {@code CraftMetaBook} 互不继承，两个目标都要挂
 * （checklist §1.6 r）；多目标 mixin 的每个 {@code @Shadow} 都必须写 {@code remap = false}。
 */
@Mixin(value = {CraftMetaBook.class, org.bukkit.craftbukkit.v.inventory.CraftMetaBookSigned.class},
       remap = false)
public abstract class CraftMetaBookAdventureApiMixin {

    @Shadow(remap = false)
    public abstract List<String> getPages();

    @Shadow(remap = false)
    public abstract void setPages(List<String> pages);

    @Unique
    public List<Component> pages() {
        List<Component> out = new ArrayList<>();
        for (String page : getPages()) {
            out.add(LegacyComponentSerializer.legacySection().deserialize(page));
        }
        return out;
    }

    @Unique
    public Book pages(List<Component> pages) {
        List<String> out = new ArrayList<>(pages.size());
        for (Component page : pages) {
            out.add(LegacyComponentSerializer.legacySection().serialize(page));
        }
        setPages(out);
        return (Book) (Object) this;
    }

    @Unique
    public Book title(Component title) {
        return (Book) ((BookMeta) (Object) this).title(title);
    }

    @Unique
    public Book author(Component author) {
        return (Book) ((BookMeta) (Object) this).author(author);
    }
}
