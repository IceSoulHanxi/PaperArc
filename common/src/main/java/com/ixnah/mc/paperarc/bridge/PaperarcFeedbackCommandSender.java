package com.ixnah.mc.paperarc.bridge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

/**
 * {@code Server#createCommandSender(Consumer)} 的 sync-fallback 实现：在真实控制台
 * sender 上套一层接口代理，把 {@code sendMessage} 的各个重载改投给 feedback consumer，
 * 其余方法（含带 chat-type 上界的 Audience 重载）原样转给控制台。
 *
 * <p>放在 {@code bridge} 而不是写成 mixin 里的 lambda：lambda 的 invokedynamic 引导方法
 * 引用 mixin 类自身，合并进 {@code CraftServer} 后既踩 Mixin "目标类不得引用 mixin 包内的类"，
 * 也会让合成类的 InnerClasses/NestHost 与目标类互相矛盾。</p>
 */
public final class PaperarcFeedbackCommandSender implements InvocationHandler {

    private final ConsoleCommandSender console;
    private final Consumer<? super Component> feedback;

    private PaperarcFeedbackCommandSender(ConsoleCommandSender console, Consumer<? super Component> feedback) {
        this.console = console;
        this.feedback = feedback;
    }

    public static CommandSender create(ConsoleCommandSender console, Consumer<? super Component> feedback) {
        return (CommandSender) Proxy.newProxyInstance(
            PaperarcFeedbackCommandSender.class.getClassLoader(),
            new Class<?>[] {ConsoleCommandSender.class},
            new PaperarcFeedbackCommandSender(console, feedback)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if ("sendMessage".equals(name) && args != null && args.length >= 1) {
            Object first = args[0];
            if (first instanceof Component component) {
                this.feedback.accept(component);
                return null;
            }
            if (first instanceof ComponentLike like) {
                this.feedback.accept(like.asComponent());
                return null;
            }
            if (first instanceof String legacy) {
                this.feedback.accept(LegacyComponentSerializer.legacySection().deserialize(legacy));
                return null;
            }
            if (first instanceof String[] legacies) {
                for (String line : legacies) {
                    this.feedback.accept(LegacyComponentSerializer.legacySection().deserialize(line));
                }
                return null;
            }
        } else if ("toString".equals(name)) {
            return "PaperArcCommandSender";
        }
        try {
            return method.invoke(this.console, args);
        } catch (InvocationTargetException e) {
            throw e.getCause() == null ? e : e.getCause();
        }
    }
}
