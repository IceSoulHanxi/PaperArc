package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.LightningBolt;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLightningStrike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;

/**
 * Port of Paper's More-lightning-API additions on
 * {@link CraftLightningStrike}: {@code getFlashCount()},
 * {@code setFlashCount(int)} and {@code getCausingEntity()}.
 *
 * Vanilla NMS keeps the {@code flashes} counter private (Paper widens it via
 * AT), so it is reached reflectively; {@code getCause()} is public vanilla.
 */
@Mixin(CraftLightningStrike.class)
public abstract class CraftLightningStrikeApiMixin {

    @Unique
    private static volatile Field PAPERARC$FLASHES_FIELD;

    @Shadow
    public abstract LightningBolt getHandle();

    @Unique
    private static Field paperarc$flashesField() {
        Field field = PAPERARC$FLASHES_FIELD;
        if (field == null) {
            synchronized (CraftLightningStrikeApiMixin.class) {
                if (PAPERARC$FLASHES_FIELD == null) {
                    try {
                        Field resolved = LightningBolt.class.getDeclaredField("flashes");
                        resolved.setAccessible(true);
                        PAPERARC$FLASHES_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS LightningBolt.flashes field not found", e);
                    }
                }
                field = PAPERARC$FLASHES_FIELD;
            }
        }
        return field;
    }

    @Unique
    public int getFlashCount() {
        try {
            return paperarc$flashesField().getInt(this.getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS LightningBolt.flashes", e);
        }
    }

    @Unique
    public void setFlashCount(int flashes) {
        Preconditions.checkArgument(flashes >= 0, "Flashes has to be a positive number!");
        try {
            paperarc$flashesField().setInt(this.getHandle(), flashes);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS LightningBolt.flashes", e);
        }
    }

    @Unique
    public org.bukkit.entity.Entity getCausingEntity() {
        net.minecraft.server.level.ServerPlayer cause = this.getHandle().getCause();
        return cause == null ? null : com.ixnah.mc.paperarc.bridge.PaperArcBridge.bukkitEntity(cause);
    }
}
