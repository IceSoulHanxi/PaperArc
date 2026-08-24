package com.ixnah.mc.paperarc.bridge;

import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Cross-mixin hand-off of the pre-change stack between the container-menu
 * remote-slot mixins. Kept outside mixin classes because Sponge Mixin only
 * allows private added statics.
 */
public final class MenuSlotState {

    public static final ThreadLocal<ItemStack> OLD_STACK = new ThreadLocal<>();

    private MenuSlotState() {
    }

    public static <R> R withOldStack(ItemStack value, Function<ThreadLocal<ItemStack>, R> action) {
        OLD_STACK.set(value);
        try {
            return action.apply(OLD_STACK);
        } finally {
            OLD_STACK.remove();
        }
    }
}
