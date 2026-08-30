package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftFirework;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Firework API missing from Arclight's CraftFirework.
 *
 * Paper reaches {@code FireworkRocketEntity.life}/{@code lifetime} and the private
 * synched-data accessor {@code DATA_ID_FIREWORKS_ITEM} through access transformers;
 * all are widened via AT (f_37022_ / f_37023_ / f_37019_) and accessed directly —
 * no reflection. {@code spawningEntity} is a field Paper injects into the NMS class
 * at launch time and persists via NBT — that storage does not exist at runtime, so
 * the value is derived from the projectile owner instead.
 */
@Mixin(CraftFirework.class)
public abstract class CraftFireworkApiMixin {

    @Shadow
    public abstract FireworkRocketEntity getHandle();

    @Shadow
    public abstract FireworkMeta getFireworkMeta();

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
        handle.getEntityData().set(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM, nmsItem);
    }

    @Unique
    public int getTicksFlown() {
        return getHandle().life;
    }

    @Unique
    public void setTicksFlown(int ticks) {
        getHandle().life = ticks;
    }

    @Unique
    public int getTicksToDetonate() {
        return getHandle().lifetime;
    }

    @Unique
    public void setTicksToDetonate(int ticks) {
        getHandle().lifetime = ticks;
    }
}
