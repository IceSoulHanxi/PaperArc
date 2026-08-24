package dev.paperarc.mixin.common.api;

import java.lang.reflect.Field;
import java.util.UUID;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.craftbukkit.v.entity.CraftFirework;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import dev.paperarc.bridge.craft.CraftEntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Firework API missing from Arclight's CraftFirework.
 *
 * Paper reaches {@code FireworkRocketEntity.life}/{@code lifetime} and the private
 * synched-data accessor {@code DATA_ID_FIREWORKS_ITEM} through access transformers;
 * none of those are widened in the runtime NMS, so they are read reflectively here.
 * {@code spawningEntity} is a field Paper injects into the NMS class at launch time
 * and persists via NBT — that storage does not exist at runtime, so the value is
 * derived from the projectile owner instead.
 */
@Mixin(CraftFirework.class)   public abstract FireworkRocketEntity getHandle();

    @Shadow
    public abstract FireworkMeta getFireworkMeta();

    @Unique
    private static volatile Field PAPERARC$ITEM_ACCESSOR_FIELD;
    @Unique
    private static volatile Field PAPERARC$LIFE_FIELD;
    @Unique
    private static volatile Field PAPERARC$LIFETIME_FIELD;

    @Unique
    @SuppressWarnings("unchecked")
    private static EntityDataAccessor<ItemStack> paperarc$itemAccessor() {
        Field f = PAPERARC$ITEM_ACCESSOR_FIELD;
        if (f == null) {
            synchronized (CraftFireworkApiMixin.class) {
                if (PAPERARC$ITEM_ACCESSOR_FIELD == null) {
                    try {
                        Field resolved = FireworkRocketEntity.class.getDeclaredField("DATA_ID_FIREWORKS_ITEM");
                        resolved.setAccessible(true);
                        PAPERARC$ITEM_ACCESSOR_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException(
                                "NMS FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM not found", e);
                    }
                }
                f = PAPERARC$ITEM_ACCESSOR_FIELD;
            }
        }
        try {
            return (EntityDataAccessor<ItemStack>) f.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read firework item data accessor", e);
        }
    }

    @Unique
    private static Field paperarc$resolveCounter(String name) {
        boolean isLife = name.equals("life");
        Field f = isLife ? PAPERARC$LIFE_FIELD : PAPERARC$LIFETIME_FIELD;
        if (f != null) {
            return f;
        }
        synchronized (CraftFireworkApiMixin.class) {
            try {
                Field resolved = FireworkRocketEntity.class.getDeclaredField(name);
                resolved.setAccessible(true);
                if (isLife) {
                    PAPERARC$LIFE_FIELD = resolved;
                } else {
                    PAPERARC$LIFETIME_FIELD = resolved;
                }
                return resolved;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("NMS FireworkRocketEntity." + name + " not found", e);
            }
        }
    }

    @Unique
    private void paperarc$writeCounter(String name, int value) {
        try {
            paperarc$resolveCounter(name).setInt(getHandle(), value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS FireworkRocketEntity." + name, e);
        }
    }

    /**
     * Paper records the shooter UUID into a dedicated NMS field (and NBT) at launch;
     * the runtime NMS has neither, so fall back to the projectile owner (set for
     * crossbow-launched rockets; hand-launched vanilla rockets carry no owner).
     */
    @Unique
    public UUID getSpawningEntity() {
        Entity owner = getHandle().getOwner();
        return owner == null ? null : owner.getUUID();
    }

    @Unique
    public org.bukkit.inventory.ItemStack getItem() {
        return CraftItemStack.asBukkitCopy(getHandle().getItem());
    }

    @Unique
    public void setItem(org.bukkit.inventory.ItemStack itemStack) {
        FireworkRocketEntity handle = getHandle();
        // Mirror Paper: keep the current firework effects on whatever item is swapped in.
        FireworkMeta meta = getFireworkMeta();
        ItemStack nmsItem = itemStack == null
                ? new ItemStack(Items.FIREWORK_ROCKET)
                : CraftItemStack.asNMSCopy(itemStack);
        CraftItemStack.setItemMeta(nmsItem, meta); // replicates Paper's applyFireworkEffect
        handle.getEntityData().set(paperarc$itemAccessor(), nmsItem);
    }

    @Unique
    public int getTicksFlown() {
        try {
            return paperarc$resolveCounter("life").getInt(getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS FireworkRocketEntity.life", e);
        }
    }

    @Unique
    public void setTicksFlown(int ticks) {
        paperarc$writeCounter("life", ticks);
    }

    @Unique
    public int getTicksToDetonate() {
        try {
            return paperarc$resolveCounter("lifetime").getInt(getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS FireworkRocketEntity.lifetime", e);
        }
    }

    @Unique
    public void setTicksToDetonate(int ticks) {
        paperarc$writeCounter("lifetime", ticks);
    }
}
