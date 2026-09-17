package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v.entity.CraftThrownPotion;
import org.bukkit.craftbukkit.v.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/**
 * Port of Paper's More-Projectile-API additions on {@link CraftThrownPotion}:
 * {@code getPotionMeta()}, {@code setPotionMeta(PotionMeta)} and {@code splash()}.
 *
 * <p>Deviations from Paper (1.20.1 CraftBukkit has no Paper-only helpers):
 * <ul>
 *   <li>Paper uses {@code CraftItemStack.getItemMeta(stack, Material.SPLASH_POTION)} to force the
 *       meta type; 1.20.1 only has the single-argument overload, so the meta type is derived from
 *       the item itself and coerced to a fresh SPLASH_POTION meta when it is not a
 *       {@link PotionMeta} (empty / non-potion item).</li>
 *   <li>Paper uses {@code CraftItemStack.applyMetaToItem}; the stock equivalent here is the static
 *       {@code CraftItemStack.setItemMeta(nmsStack, meta)}.</li>
 *   <li>Paper extracts a public {@code ThrownPotion#splash(HitResult)} out of {@code onHit};
 *       vanilla 1.20.1 keeps that logic inline and private, so the same branch structure is
 *       replayed here with a null hit result. The four vanilla helpers are widened by AT
 *       (m_37552_/m_37553_/m_37537_/m_37547_ in META-INF/accesstransformer.cfg) and called
 *       directly — no string reflection, which cannot work on the srg runtime.</li>
 * </ul>
 */
@Mixin(CraftThrownPotion.class)
public abstract class CraftThrownPotionApiMixin {

    @Shadow
    public abstract ThrownPotion getHandle();

    @Unique
    public PotionMeta getPotionMeta() {
        ItemMeta meta = CraftItemStack.getItemMeta(this.getHandle().getItem());
        if (meta instanceof PotionMeta) {
            return (PotionMeta) meta;
        }
        return (PotionMeta) CraftItemFactory.instance().getItemMeta(Material.SPLASH_POTION);
    }

    @Unique
    public void setPotionMeta(PotionMeta meta) {
        Preconditions.checkArgument(meta != null, "meta cannot be null");
        net.minecraft.world.item.ItemStack item = this.getHandle().getItem();
        CraftItemStack.setItemMeta(item, meta);
        this.getHandle().setItem(item); // Reset item (as in Paper)
    }

    /** Replays vanilla {@code ThrownPotion#onHit} effect application with a null hit result. */
    @Unique
    public void splash() {
        ThrownPotion handle = this.getHandle();
        if (handle.level().isClientSide) {
            return;
        }
        net.minecraft.world.item.ItemStack itemstack = handle.getItem();
        Potion potion = PotionUtils.getPotion(itemstack);
        List<MobEffectInstance> effects = PotionUtils.getMobEffects(itemstack);

        if (potion == Potions.WATER && effects.isEmpty()) {
            handle.applyWater();
        } else if (!effects.isEmpty()) {
            if (handle.isLingering()) {
                handle.makeAreaOfEffectCloud(itemstack, potion);
            } else {
                // Paper passes null when there is no hit result (splash() entry point)
                handle.applySplash(effects, (net.minecraft.world.entity.Entity) null);
            }
        }

        int event = potion.hasInstantEffects() ? 2007 : 2002;
        handle.level().levelEvent(event, handle.blockPosition(), PotionUtils.getColor(itemstack));
        handle.discard();
    }
}
