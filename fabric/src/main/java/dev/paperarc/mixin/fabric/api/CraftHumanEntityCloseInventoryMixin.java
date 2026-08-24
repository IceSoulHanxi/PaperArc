package dev.paperarc.mixin.fabric.api;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Fabric-side implementation of Paper's HumanEntity#closeInventory(Reason).
 * Companion of HumanEntityCloseInventoryIfaceMixin; kept out of the common
 * config for the same NeoForge early-apply descriptor-visibility reason.
 *
 * spigot NMS lacks ServerPlayer.closeContainer(Reason); the reason is not
 * plumbed into the internally-fired InventoryCloseEvent. Player.closeContainer()
 * is public in vanilla but protected in spigot NMS, so a MethodHandle built via
 * privateLookupIn is used (JIT-inlinable, per project convention).
 */
@Mixin(CraftHumanEntity.class)
public abstract class CraftHumanEntityCloseInventoryMixin {

    @Unique
    private static volatile MethodHandle PAPERARC$CLOSE_CONTAINER;

    @Unique
    public void closeInventory(InventoryCloseEvent.Reason reason) {
        try {
            MethodHandle handle = PAPERARC$CLOSE_CONTAINER;
            if (handle == null) {
                synchronized (CraftHumanEntityCloseInventoryMixin.class) {
                    if (PAPERARC$CLOSE_CONTAINER == null) {
                        PAPERARC$CLOSE_CONTAINER = MethodHandles.privateLookupIn(Player.class, MethodHandles.lookup())
                                .findVirtual(Player.class, "closeContainer",
                                        MethodType.methodType(void.class));
                    }
                    handle = PAPERARC$CLOSE_CONTAINER;
                }
            }
            handle.invokeExact(((org.bukkit.craftbukkit.v.entity.CraftPlayer) (Object) this).getHandle());
        } catch (Throwable t) {
            throw new IllegalStateException("Cannot invoke Player.closeContainer", t);
        }
    }
}
