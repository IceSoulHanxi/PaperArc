package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.scoreboard.DisplaySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让 {@code DisplaySlotEnumMixin} 补出来的 {@code SIDEBAR_TEAM_<色>} 真的能用：
 * 折算成运行时 {@code CraftScoreboardTranslations.SLOTS} 里认识的旧名 {@code SIDEBAR_<色>}。
 *
 * <p>为什么不能直接往 {@code SLOTS} 里加：它是 {@code ImmutableBiMap}，
 * 新旧两个常量对应同一个槽位名 {@code "sidebar.team.red"}，BiMap 不允许值重复。
 *
 * <p>递归调用自身是安全的：折算出来的旧名不以 {@code SIDEBAR_TEAM_} 开头，
 * 第二次进来直接走原方法体。
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.scoreboard.CraftScoreboardTranslations", remap = false)
public abstract class CraftScoreboardSlotAliasMixin {

    @Shadow(remap = false)
    static int fromBukkitSlot(DisplaySlot slot) {
        throw new AssertionError();
    }

    @Inject(method = "fromBukkitSlot(Lorg/bukkit/scoreboard/DisplaySlot;)I",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void paperarc$aliasPaperSlot(DisplaySlot slot, CallbackInfoReturnable<Integer> cir) {
        if (slot == null) {
            return;
        }
        String name = slot.name();
        if (!name.startsWith("SIDEBAR_TEAM_")) {
            return;
        }
        DisplaySlot legacy;
        try {
            legacy = DisplaySlot.valueOf("SIDEBAR_" + name.substring("SIDEBAR_TEAM_".length()));
        } catch (IllegalArgumentException ignored) {
            return;
        }
        cir.setReturnValue(fromBukkitSlot(legacy));
    }
}
