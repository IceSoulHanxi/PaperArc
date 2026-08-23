package dev.paperarc.mixin.common.api;

import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.craftbukkit.v.inventory.view.CraftAnvilView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's AnvilView enchantment-level-restriction bypass API.
 *
 * Paper stores the flag in a new public AnvilMenu field and checks it inside
 * createResult; vanilla 1.21.1 NMS has no such field, so the state is kept in
 * the ApiState side map keyed by the menu (the anvil result computation is NOT
 * gated by this flag).
 */
@Mixin(CraftAnvilView.class)
public abstract class CraftAnvilViewApiMixin {

    @Unique
    private static final String PAPERARC$BYPASS = "paperarc:bypassEnchantmentLevelRestriction";

    @Shadow
    @Final
    private AnvilMenu container;

    @Unique
    public boolean bypassesEnchantmentLevelRestriction() {
        return dev.paperarc.bridge.ApiState.get(container, PAPERARC$BYPASS, Boolean.FALSE);
    }

    @Unique
    public void bypassEnchantmentLevelRestriction(boolean bypassEnchantmentLevelRestriction) {
        dev.paperarc.bridge.ApiState.put(container, PAPERARC$BYPASS, bypassEnchantmentLevelRestriction);
    }
}
