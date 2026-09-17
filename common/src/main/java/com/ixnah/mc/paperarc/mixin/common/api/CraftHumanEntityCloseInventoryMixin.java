package com.ixnah.mc.paperarc.mixin.common.api;

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
 * RuntimeClassInjector 现在会在 mixin 应用之前把这些 bukkit 类型的字节喂给
 * 字节码 provider（Forge/NeoForge 走 transformerLoader hook，Fabric 由产物直接携带），
 * 描述符可解析，因此本类从 fabric 专属回归 common，三端统一生效（B5）。
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
