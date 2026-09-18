package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code BlockCookEvent#getRecipe()}（{@code FurnaceSmeltEvent} 继承它）。
 *
 * <p>paper 把配方做成构造器形参；运行时的两个构造点（营火 {@code cookTick} 与
 * 熔炉 {@code burn}）用的都是三参老签名，改成由触发点压 ThreadLocal、构造器取回。
 * 两个触发点见 {@code block.CampfireBlockEntityCookRecipeMixin} /
 * {@code block.AbstractFurnaceSmeltRecipeMixin}。
 *
 * <p>{@code FurnaceSmeltEvent} 走的是父类这个构造器，所以一处覆盖两个事件。
 */
@Mixin(BlockCookEvent.class)
public abstract class BlockCookEventApiMixin {

    @Unique
    private CookingRecipe<?> paperarc$recipe;

    @Inject(method = "<init>(Lorg/bukkit/block/Block;Lorg/bukkit/inventory/ItemStack;Lorg/bukkit/inventory/ItemStack;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureRecipe(Block block, ItemStack source, ItemStack result, CallbackInfo ci) {
        this.paperarc$recipe = PaperarcEventCauses.takeBlockCookRecipe();
    }

    @Unique
    public CookingRecipe<?> getRecipe() {
        return this.paperarc$recipe;
    }
}
