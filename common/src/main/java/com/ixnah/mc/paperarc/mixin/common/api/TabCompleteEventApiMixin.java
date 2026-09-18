package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.server.TabCompleteEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code TabCompleteEvent#isCommand()/getLocation()}。
 *
 * <p>paper 把两者做成构造器形参；运行时只有三参构造器（`javap -p` 核对 Arclight 1.21.1），
 * 而 paper 的三参构造器自己就是这么算的：
 * {@code isCommand = sender instanceof ConsoleCommandSender || buffer.startsWith("/")}、
 * {@code loc = null}。所以这里按同一公式现算，取值与 Paper 完全一致，不需要触发点配合。
 */
@Mixin(TabCompleteEvent.class)
public abstract class TabCompleteEventApiMixin {

    @Unique
    public boolean isCommand() {
        TabCompleteEvent self = (TabCompleteEvent) (Object) this;
        return self.getSender() instanceof ConsoleCommandSender || self.getBuffer().startsWith("/");
    }

    @Unique
    public Location getLocation() {
        return null;
    }
}
