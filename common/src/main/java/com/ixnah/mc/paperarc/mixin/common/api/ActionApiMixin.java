package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.event.block.Action;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 {@code Action#isLeftClick()/isRightClick()}（A6/X-2 第六批，纯枚举判定）。 */
@Mixin(Action.class)
public abstract class ActionApiMixin {

    @Unique
    public boolean isLeftClick() {
        Action self = (Action) (Object) this;
        return self == Action.LEFT_CLICK_BLOCK || self == Action.LEFT_CLICK_AIR;
    }

    @Unique
    public boolean isRightClick() {
        Action self = (Action) (Object) this;
        return self == Action.RIGHT_CLICK_BLOCK || self == Action.RIGHT_CLICK_AIR;
    }
}
