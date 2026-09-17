package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.ServerPlayerBridge;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code ServerPlayer.loginTime}. Mixin merges field
 * initialisers into the target's constructors, and ServerPlayer is constructed
 * when the connection is accepted, so the wall clock taken here is the login
 * time (same point Paper records it).
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerFieldsMixin implements ServerPlayerBridge {

    @Unique
    public long loginTime = System.currentTimeMillis(); // Paper

    @Override
    public long paper$loginTime() {
        return this.loginTime;
    }
}
