package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.horse.Llama;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftLlama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Missing-Entity-API caravan additions on {@link CraftLlama}.
 *
 * All methods delegate to public NMS members except {@link #getCaravanTail()},
 * which Paper implements by reading the private NMS field
 * {@code Llama.caravanTail} — a Craft-host mixin cannot shadow NMS privates,
 * so that field is accessed reflectively (mojmap runtime name: caravanTail).
 */
@Mixin(CraftLlama.class)
public abstract class CraftLlamaApiMixin {

    @Shadow
    public abstract Llama getHandle();

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$CARAVAN_TAIL_FIELD;

    @Unique
    private static java.lang.reflect.Field paperarc$caravanTailField() {
        java.lang.reflect.Field f = PAPERARC$CARAVAN_TAIL_FIELD;
        if (f == null) {
            synchronized (CraftLlamaApiMixin.class) {
                if (PAPERARC$CARAVAN_TAIL_FIELD == null) {
                    try {
                        java.lang.reflect.Field resolved = Llama.class.getDeclaredField("caravanTail");
                        resolved.setAccessible(true);
                        PAPERARC$CARAVAN_TAIL_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Llama.caravanTail field not found", e);
                    }
                    f = PAPERARC$CARAVAN_TAIL_FIELD;
                }
            }
        }
        return f;
    }

    @Unique
    private CraftServer paperarc$server() {
        return (CraftServer) ((CraftEntity) (Object) this).getServer();
    }

    // Paper start - Missing Entity API
    @Unique
    public boolean inCaravan() {
        return this.getHandle().inCaravan();
    }

    @Unique
    public org.bukkit.entity.Llama getCaravanHead() {
        Llama head = this.getHandle().getCaravanHead();
        return head == null ? null : (org.bukkit.entity.Llama) CraftEntity.getEntity(paperarc$server(), head);
    }

    @Unique
    public boolean hasCaravanTail() {
        return this.getHandle().hasCaravanTail();
    }

    @Unique
    public org.bukkit.entity.Llama getCaravanTail() {
        Llama tail;
        try {
            tail = (Llama) paperarc$caravanTailField().get(this.getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS Llama.caravanTail", e);
        }
        return tail == null ? null : (org.bukkit.entity.Llama) CraftEntity.getEntity(paperarc$server(), tail);
    }

    @Unique
    public void joinCaravan(org.bukkit.entity.Llama llama) {
        this.getHandle().joinCaravan(((CraftLlama) llama).getHandle());
    }

    @Unique
    public void leaveCaravan() {
        this.getHandle().leaveCaravan();
    }
    // Paper end - Missing Entity API
}
