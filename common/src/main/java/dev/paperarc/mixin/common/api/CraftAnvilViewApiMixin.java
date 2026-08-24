package dev.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.inventory.view.CraftAnvilView;
import dev.paperarc.bridge.craft.CraftInventoryViewBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's AnvilView enchantment-level-restriction bypass API.
 *
 * Paper stores the flag in a new public AnvilMenu field and checks it inside
 * createResult; vanilla 1.21.1 NMS has no such field, so the state is kept in
 * the ApiState side map keyed by the menu (the anvil result computation is NOT
 * gated by this flag). The menu is reached through the
 * {@link CraftInventoryViewBridge} duck interface merged onto the generic
 * view base.
 */
@Mixin(CraftAnvilView.class)
public abstract class CraftAnvilViewApiMixin {

    @Unique
    private static final String PAPERARC$BYPASS = "paperarc:bypassEnchantmentLevelRestriction";

    @Unique
    public boolean bypassesEnchantmentLevelRestriction() {
        return dev.paperarc.bridge.ApiState.get(
            ((CraftInventoryViewBridge) (Object) this).paperarc$menu(), PAPERARC$BYPASS, Boolean.FALSE);
    }

    @Unique
    public void bypassEnchantmentLevelRestriction(boolean bypassEnchantmentLevelRestriction) {
        dev.paperarc.bridge.ApiState.put(
            ((CraftInventoryViewBridge) (Object) this).paperarc$menu(), PAPERARC$BYPASS, bypassEnchantmentLevelRestriction);
    }
}
