package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.event.block.Action;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 加在 {@code Action} 枚举上的 2 个便利方法（checklist §1.10 am，第 ⑤ 批）。 */
@Mixin(Action.class)
public abstract class ActionApiMixin {

    @Unique
    private Action paperarc$self() {
        return (Action) (Object) this;
    }

    @Unique
    public boolean isLeftClick() {
        Action self = this.paperarc$self();
        return self == Action.LEFT_CLICK_AIR || self == Action.LEFT_CLICK_BLOCK;
    }

    @Unique
    public boolean isRightClick() {
        Action self = this.paperarc$self();
        return self == Action.RIGHT_CLICK_AIR || self == Action.RIGHT_CLICK_BLOCK;
    }
}
