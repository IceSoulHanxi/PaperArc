package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.block.CraftBlockType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** B2-6：{@code BlockType extends Translatable} 的终端方法。 */
@Mixin(CraftBlockType.class)
public abstract class CraftBlockTypeApiMixin {

    @Shadow
    public abstract net.minecraft.world.level.block.Block getHandle();

    @Unique
    public String translationKey() {
        return this.getHandle().getDescriptionId();
    }

    /**
     * B2-6 顺带补：{@code BlockTypeIfaceMixin} 从 B4 起就声明了 {@code hasCollision()}，
     * 但一直没有实现体（PARTIAL_IMPL/NO_IMPL 门禁补上口径后才暴露）。
     */
    @Unique
    public boolean hasCollision() {
        return this.getHandle().hasCollision;
    }
}
