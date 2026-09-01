package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.bukkit.craftbukkit.v.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.List;

/**
 * Port of Paper's additions on {@link CraftInventory}:
 * {@code close()} and {@code removeItemAnySlot(ItemStack...)}.
 *
 * Both are implemented against the craft-level API surface
 * ({@code getViewers()} / {@code getContents()} / {@code getItem(int)} /
 * {@code setItem(int, ItemStack)} / {@code clear(int)}, all javap-verified on
 * the base jar), so no NMS types are required.
 *
 * The third slice method {@code getHolder(boolean)} delegates to the existing
 * {@link #getHolder()}: Arclight's CraftInventory#getHolder() already builds a
 * live holder on every call (no snapshot cache exists), so both flags are
 * semantically equivalent here — the snapshot distinction only matters for
 * Paper's TileStateInventoryHolder path, which this base jar lacks.
 */
@Mixin(CraftInventory.class)
public abstract class CraftInventoryApiMixin {

    @Shadow
    public abstract List<HumanEntity> getViewers();

    @Shadow
    public abstract ItemStack[] getContents();

    @Shadow
    public abstract ItemStack getItem(int slot);

    @Shadow
    public abstract void setItem(int slot, ItemStack item);

    @Shadow
    public abstract void clear(int index);

    @Shadow
    public abstract InventoryHolder getHolder();

    /**
     * Paper: {@code getHolder(boolean useSnapshot)}。Arclight 的 getHolder()
     * 本就即时构造 live holder（无快照缓存），快照语义在此实现下不存在，
     * 两种取值返回同一对象 —— 语义等同，直接复用现有逻辑。
     */
    @Unique
    public InventoryHolder getHolder(boolean useSnapshot) {
        return this.getHolder();
    }

    /**
     * Paper: closes the inventory for all viewers and returns how many
     * viewers it was closed for. Iterates a snapshot copy of the viewer list,
     * exactly like the original implementation.
     */
    @Unique
    public int close() {
        int count = this.getViewers().size();
        Lists.newArrayList(this.getViewers()).forEach(HumanEntity::closeInventory);
        return count;
    }

    /**
     * Paper: like {@code removeItem(ItemStack...)}, but searches the ENTIRE
     * inventory contents (all slots) instead of only the storage contents.
     * Mirrors Paper's {@code removeItem(boolean searchEntire, ItemStack...)}
     * refactor with {@code searchEntire = true}.
     */
    @Unique
    public HashMap<Integer, ItemStack> removeItemAnySlot(ItemStack... items) {
        Preconditions.checkArgument(items != null, "items cannot be null");
        HashMap<Integer, ItemStack> leftover = new HashMap<>();

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            int toDelete = item.getAmount();

            while (true) {
                // Paper start - Allow searching entire contents
                int first = this.paperarc$firstAnySlot(item, this.getContents());
                // Paper end

                // Drat! we don't have this type in the inventory
                if (first == -1) {
                    item.setAmount(toDelete);
                    leftover.put(i, item);
                    break;
                }

                ItemStack itemStack = this.getItem(first);
                int amount = itemStack.getAmount();

                if (amount <= toDelete) {
                    toDelete -= amount;
                    // clear the slot, all used up
                    this.clear(first);
                } else {
                    // split the stack and set
                    itemStack.setAmount(amount - toDelete);
                    this.setItem(first, itemStack);
                    toDelete = 0;
                }
            }
        }

        return leftover;
    }

    /**
     * Copy of CraftInventory's private {@code first(ItemStack, boolean)} with
     * the array parameter that Paper's refactor introduces; the amount flag is
     * fixed to {@code false} ({@code isSimilar}, ignoring stack size) because
     * both remove paths call it that way.
     */
    @Unique
    private static int paperarc$firstAnySlot(ItemStack item, ItemStack[] inventory) {
        if (item == null) {
            return -1;
        }
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                continue;
            }
            if (item.isSimilar(inventory[i])) {
                return i;
            }
        }
        return -1;
    }
}
