package io.izzel.arclight.common.mod.server.event;

import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 编译期桩（不进产物）：只为让 {@code @Mixin(targets = …)} 能解析到目标类。
 * 签名与 Arclight 1.20.1（{@code 6de9fecc}）一致。
 */
public abstract class ArclightEventFactory {

    public static void callEvent(Event event) {
        throw new AssertionError("stub");
    }

    public static void callEntityDeathEvent(LivingEntity entity, List<ItemStack> drops) {
        throw new AssertionError("stub");
    }
}
