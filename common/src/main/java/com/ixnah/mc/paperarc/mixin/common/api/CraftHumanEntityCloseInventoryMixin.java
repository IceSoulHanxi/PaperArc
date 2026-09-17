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
    public void closeInventory(InventoryCloseEvent.Reason reason) {
        // vanilla Player#closeContainer() 本来就是 public，之前那层 MethodHandle 是多余的
        ((org.bukkit.craftbukkit.v.entity.CraftPlayer) (Object) this).getHandle().closeContainer();
    }
}
