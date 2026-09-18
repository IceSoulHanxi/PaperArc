package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code org.bukkit.command.Command} 上的方法（A6/X-2 第五批）。
 * Component 版走 legacy section 序列化桥到 Bukkit 自己的 String 版（本仓库其它
 * Component↔String 桥同一路子），两个 static 由 markSyntheticStaticApi 打 ACC_SYNTHETIC。
 */
@Mixin(Command.class)
public abstract class CommandApiMixin {

    @Unique
    private Command paperarc$self() {
        return (Command) (Object) this;
    }

    @Unique
    public String getTimingName() {
        return this.paperarc$self().getName();
    }

    @Unique
    public Component permissionMessage() {
        String legacy = this.paperarc$self().getPermissionMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void permissionMessage(Component permissionMessage) {
        this.paperarc$self().setPermissionMessage(permissionMessage == null ? null
                : LegacyComponentSerializer.legacySection().serialize(permissionMessage));
    }

    @Unique
    @Widen(because = "paper-api: org.bukkit.command.Command 的 public static broadcastCommandMessage")
    private static void broadcastCommandMessage(CommandSender source, Component message) {
        broadcastCommandMessage(source, message, true);
    }

    @Unique
    @Widen(because = "paper-api: org.bukkit.command.Command 的 public static broadcastCommandMessage")
    private static void broadcastCommandMessage(CommandSender source, Component message,
                                               boolean sendToSource) {
        Command.broadcastCommandMessage(source,
                LegacyComponentSerializer.legacySection().serialize(message), sendToSource);
    }
}
