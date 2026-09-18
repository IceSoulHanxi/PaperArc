package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code PlayerItemDamageEvent#getOriginalDamage()}。
 *
 * <p>paper 的三参构造器就是 {@code this(player, what, damage, damage)} —— 构造完
 * {@code originalDamage == damage}，之后只有 {@code setDamage} 会改 {@code damage}。
 * 运行时只有三参构造器，所以在它的 RETURN 处快照一次即等价。
 */
@Mixin(PlayerItemDamageEvent.class)
public abstract class PlayerItemDamageEventApiMixin {

    @Unique
    private int paperarc$originalDamage;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/inventory/ItemStack;I)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$snapshotOriginalDamage(Player player, ItemStack what, int damage, CallbackInfo ci) {
        this.paperarc$originalDamage = damage;
    }

    @Unique
    public int getOriginalDamage() {
        return this.paperarc$originalDamage;
    }
}
