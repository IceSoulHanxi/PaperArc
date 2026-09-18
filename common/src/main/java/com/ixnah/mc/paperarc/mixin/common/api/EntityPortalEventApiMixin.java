package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityPortalEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper 的 {@code EntityPortalEvent#getPortalType}。
 *
 * <p>Paper 在调用点按传送门方块类型传进来；Arclight 的
 * {@code EntityMixin#callPortalEvent} 只有 {@code TeleportCause}，改不了事件形参。
 * 这里在构造器里按实体当前所站方块判定 —— 那正是触发这次传送的传送门方块，
 * 与 Paper 的取值来源一致，不是写死常量。
 */
@Mixin(EntityPortalEvent.class)
public abstract class EntityPortalEventApiMixin {

    @Unique
    private PortalType paperarc$portalType;

    @Inject(method = "<init>(Lorg/bukkit/entity/Entity;Lorg/bukkit/Location;Lorg/bukkit/Location;I)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$capturePortalType(Entity entity, Location from, Location to, int searchRadius,
                                            CallbackInfo ci) {
        this.paperarc$portalType = paperarc$detect(from);
    }

    @Unique
    private static PortalType paperarc$detect(Location from) {
        if (from == null || from.getWorld() == null) {
            return PortalType.CUSTOM;
        }
        Material type = from.getBlock().getType();
        if (type == Material.NETHER_PORTAL) {
            return PortalType.NETHER;
        }
        if (type == Material.END_PORTAL || type == Material.END_GATEWAY) {
            return PortalType.ENDER;
        }
        return PortalType.CUSTOM;
    }

    @Unique
    public PortalType getPortalType() {
        PortalType type = this.paperarc$portalType;
        return type == null ? PortalType.CUSTOM : type;
    }
}
