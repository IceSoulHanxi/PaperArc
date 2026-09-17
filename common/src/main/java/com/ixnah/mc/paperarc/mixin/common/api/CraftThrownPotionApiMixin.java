package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.projectile.ThrownPotion;
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
 * branch structure is replayed via reflection into the private vanilla
 * helpers ({@code applyWater}/{@code isLingering}/
 * {@code makeAreaOfEffectCloud}/{@code applySplash}) with a null hit result,
 * exactly like Paper's {@code splash(null)}. Meta application uses the stock
 * CraftBukkit mirror + setItemMeta path instead of Paper's internal
 * {@code CraftItemStack.applyMetaToItem}.
 */
@Mixin(CraftThrownPotion.class)
public abstract class CraftThrownPotionApiMixin {

    @Shadow
    public abstract ThrownPotion getHandle();

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
        ThrownPotion handle = this.getHandle();
        if (!handle.level().isClientSide) {
            ItemStack itemstack = handle.getItem();
            PotionContents potioncontents =
                itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            // 四个 vanilla 私有 helper 由 paperarc.accesswidener 放开
            if (potioncontents.is(net.minecraft.world.item.alchemy.Potions.WATER)) {
                handle.applyWater();
            } else if (potioncontents.getAllEffects().iterator().hasNext()) {
                if (handle.isLingering()) {
                    handle.makeAreaOfEffectCloud(potioncontents);
                } else {
                    handle.applySplash(potioncontents.getAllEffects(), null);
                }
            }
        }
    }

}
