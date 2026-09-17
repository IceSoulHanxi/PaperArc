package com.ixnah.mc.paperarc.mixin.common.api;

import io.papermc.paper.math.FinePosition;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 让 {@code org.bukkit.Location} 实现 paper 的 {@code io.papermc.paper.math.FinePosition}
 * （checklist §1.8 ab）。
 *
 * <p>paper 直接让 Location 实现了 FinePosition，Arclight 运行时没有；于是所有形参是
 * {@code Position} 的 paper API（{@code World#isPositionLoaded}、
 * {@code Sign#getInteractableSideFor}、{@code Player#lookAt}…）插件传 Location 进去
 * 就是 {@code ClassCastException}/校验失败。{@code audit.py --hierarchy} 只比对接口，
 * 照不到 Location 这种类。
 *
 * <p>{@code FinePosition} 把 blockX/blockY/blockZ、isBlock、isFine、toBlock、offset
 * 全写成了 default，真正抽象的只有 {@code Position} 的 {@code x()/y()/z()} 三个，
 * 所以这里只补这三个。{@code toVector()} 之类 Location 自己已有的方法优先级更高
 * （类方法压过接口 default），语义不变。
 */
@Mixin(Location.class)
public abstract class LocationFinePositionMixin implements FinePosition {

    @Unique
    public double x() {
        return ((Location) (Object) this).getX();
    }

    @Unique
    public double y() {
        return ((Location) (Object) this).getY();
    }

    @Unique
    public double z() {
        return ((Location) (Object) this).getZ();
    }
}
