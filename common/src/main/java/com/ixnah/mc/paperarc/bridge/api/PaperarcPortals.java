package com.ixnah.mc.paperarc.bridge.api;

import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.craftbukkit.v.entity.CraftEntity;

/**
 * 传送门事件上的 {@code getPortalType()}（gaps.md §3.1 Cause/Reason 一组）。
 *
 * <p>两个事件的类型都不需要触发点配合就能定死：
 * {@code EntityPortalEnterEvent} 的 Location 就是那个传送门方块，直接看方块类型；
 * {@code EntityPortalEvent} 发生时实体正在传送门里，看它的 {@code portalProcess} 即可。
 */
public final class PaperarcPortals {

    private PaperarcPortals() {
    }

    /** 按方块类型判定（{@code EntityPortalEnterEvent} 的 Location 就是传送门方块）。 */
    public static PortalType fromBlock(Location location) {
        if (location == null || location.getWorld() == null) {
            return PortalType.CUSTOM;
        }
        Material type = location.getBlock().getType();
        return switch (type) {
            case NETHER_PORTAL -> PortalType.NETHER;
            case END_PORTAL -> PortalType.ENDER;
            case END_GATEWAY -> PortalType.END_GATEWAY;
            default -> PortalType.CUSTOM;
        };
    }

    /** 按实体正在使用的传送门判定（{@code EntityPortalEvent}）。 */
    public static PortalType fromEntity(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity craft)) {
            return PortalType.CUSTOM;
        }
        PortalProcessor processor = craft.getHandle().portalProcess;
        if (processor == null) {
            return PortalType.CUSTOM;
        }
        if (processor.isSamePortal((Portal) Blocks.NETHER_PORTAL)) {
            return PortalType.NETHER;
        }
        if (processor.isSamePortal((Portal) Blocks.END_PORTAL)) {
            return PortalType.ENDER;
        }
        if (processor.isSamePortal((Portal) Blocks.END_GATEWAY)) {
            return PortalType.END_GATEWAY;
        }
        return PortalType.CUSTOM;
    }
}
