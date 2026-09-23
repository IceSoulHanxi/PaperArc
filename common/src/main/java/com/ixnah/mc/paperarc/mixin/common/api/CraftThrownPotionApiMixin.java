package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.entity.CraftThrownPotion;
import org.bukkit.inventory.meta.PotionMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Method;

/**
 * Port of Paper's More-Projectile-API additions on {@link CraftThrownPotion}:
 * {@code getPotionMeta()}, {@code setPotionMeta(PotionMeta)} and
 * {@code splash()}.
 *
 * Paper's {@code splash()} delegates to a public NMS
 * {@code ThrownPotion#splash(@Nullable HitResult)} extracted from
 * {@code onHit}; this codebase's NMS keeps the logic private, so the same
 * branch structure is replayed through the widened vanilla helpers
 * ({@code onHitAsWater}/{@code onHitAsPotion}) with a MISS hit result at the
 * projectile's own position, matching Paper's {@code splash(null)}. Meta application uses the stock
 * CraftBukkit mirror + setItemMeta path instead of Paper's internal
 * {@code CraftItemStack.applyMetaToItem}.
 */
@Mixin(CraftThrownPotion.class)
public abstract class CraftThrownPotionApiMixin {

    @Shadow
    public abstract AbstractThrownPotion getHandle();

    @Unique
    public PotionMeta getPotionMeta() {
        // Paper: CraftItemStack.getItemMeta(item, ItemType.SPLASH_POTION);
        // single-arg overload picks the meta class from the item type itself
        return (PotionMeta) CraftItemStack.getItemMeta(this.getHandle().getItem());
    }

    @Unique
    public void setPotionMeta(PotionMeta meta) {
        Preconditions.checkArgument(meta != null, "meta cannot be null");
        net.minecraft.world.item.ItemStack nmsItem = this.getHandle().getItem();
        org.bukkit.inventory.ItemStack mirror = CraftItemStack.asCraftMirror(nmsItem.copy());
        mirror.setItemMeta(meta);
        this.getHandle().setItem(CraftItemStack.asNMSCopy(mirror)); // Reset item (as in Paper)
    }

    @Unique
    public void splash() {
        AbstractThrownPotion handle = this.getHandle();
        if (handle.level() instanceof net.minecraft.server.level.ServerLevel level) {
            ItemStack itemstack = handle.getItem();
            PotionContents potioncontents =
                itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            // onHitAsWater / onHitAsPotion 由 paperarc.accesswidener 放开；
            // 溅射/滞留分支由子类的 onHitAsPotion 自行区分（1.21.5 起拆类）
            if (potioncontents.is(net.minecraft.world.item.alchemy.Potions.WATER)) {
                handle.onHitAsWater(level);
            } else if (potioncontents.hasEffects()) {
                // Paper 的 splash(null) 以自身位置为落点、无直接命中实体：用 MISS 命中结果表达
                handle.onHitAsPotion(level, itemstack, net.minecraft.world.phys.BlockHitResult.miss(
                    handle.position(), net.minecraft.core.Direction.DOWN, handle.blockPosition()));
            }
        }
    }

}
