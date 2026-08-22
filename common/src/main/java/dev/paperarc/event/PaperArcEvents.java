package dev.paperarc.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

/**
 * 统一的事件触发入口。
 * <p>
 * 不使用 Paper 对 {@link Event#callEvent()} 的扩展（该方法由 paper-api 添加，
 * Arclight 运行时的 org.bukkit.event.Event 未必包含），改走标准
 * {@code PluginManager#callEvent}，保证在 Spigot/Bukkit 运行时上可用。
 * 与 Paper 一致：无监听器时跳过派发以省开销。
 */
public final class PaperArcEvents {

    private PaperArcEvents() {
    }

    public static <T extends Event> T fire(T event) {
        if (event.getHandlers().getRegisteredListeners().length > 0) {
            Bukkit.getPluginManager().callEvent(event);
        }
        return event;
    }
}
