package io.izzel.arclight.common.mod.server.entity;

/**
 * Arclight {@code EntityClassLookup} 的**编译期桩**，只为让 Mixin 注解处理器能验证
 * {@code server.EntityClassLookupMixin} 的 {@code @Mixin(targets = …)} 与
 * {@code method = "init"}。运行时用的永远是 Arclight 自己那份。
 *
 * <p>为什么不直接把 Arclight 的实现 jar 放进 compileClasspath：那个 jar 里打包了一整套
 * <b>运行时版</b> {@code org.bukkit.**}（933 个 craftbukkit 类 + 全部 API 类），
 * 一旦进编译类路径就会把我们要实现的 paper-api 表面盖掉 —— 所有 paper 新增方法在编译期
 * 直接消失。
 *
 * <p>这个源码集不进产物：只有 {@code main} 会被打进 jar。
 */
public class EntityClassLookup {

    public static void init() {
    }
}
