package io.izzel.arclight.common.mod.server.event;

/**
 * Arclight {@code ArclightEventFactory} 的**编译期桩**，用途同
 * {@code io.izzel.arclight.common.mod.server.entity.EntityClassLookup}：
 * 只为让 Mixin 注解处理器能验证 {@code server.ArclightEventFactoryDeathMixin} 的
 * {@code @Mixin(targets = …)} 与两个 {@code method = …} 选择器。运行时用的永远是
 * Arclight 自己那份。
 *
 * <p>形参一律写 {@code Object}：这个源码集没有 NMS / Bukkit 的编译类路径
 * （加上去会把 main 的 compileClasspath 绕成环），而我们的选择器都是**只写方法名**
 * （Arclight 自加的方法必须这么写，见 docs/mixin-conventions.md），注解处理器只核名字。
 *
 * <p>这个源码集不进产物：只有 {@code main} 会被打进 jar。
 */
public class ArclightEventFactory {

    public static Object callEvent(Object event) {
        return event;
    }

    public static Object callEntityDeathEvent(Object victim, Object source, Object drops) {
        return null;
    }

    public static Object callPlayerDeathEvent(Object victim, Object source, Object drops,
                                              int droppedExp, Object deathMessage, boolean keepInventory) {
        return null;
    }
}
