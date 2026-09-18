package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import org.bukkit.block.Block;
import org.bukkit.event.block.BrewingStartEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code BrewingStartEvent#getRecipeBrewTime()/setRecipeBrewTime(int)} 与
 * {@code getBrewingTime()/setBrewingTime(int)}。
 *
 * <p>照 paper-api 的 `javap -c` 逐条对：
 * <ul>
 *   <li>{@code getBrewingTime()/setBrewingTime(int)} 读写的就是运行时已有的
 *       {@code brewingTime} 字段（paper 的 {@code getTotalBrewTime()} 也读它、
 *       {@code setTotalBrewTime} 直接转调 {@code setBrewingTime}）——**同义转发**，
 *       消费方现成：Arclight 在 {@code BrewingStandBlockEntity} 里
 *       {@code brewTime = event.getTotalBrewTime()}（`javap` 核对）。</li>
 *   <li>{@code recipeBrewTime} 是 paper 的第二个字段，构造器里与 {@code brewingTime}
 *       取同一个初值，之后**服务端从不读它**（paper 那边也一样，它是给插件看"配方本来要多久"的）。
 *       所以这里用一个 {@code @Unique} 字段在构造器 RETURN 处快照，语义与 paper 完全一致。</li>
 * </ul>
 */
@Mixin(BrewingStartEvent.class)
public abstract class BrewingStartEventApiMixin {

    @Shadow(remap = false)
    public abstract int getTotalBrewTime();

    @Shadow(remap = false)
    public abstract void setTotalBrewTime(int brewTime);

    @Unique
    private int paperarc$recipeBrewTime;

    @Inject(method = "<init>(Lorg/bukkit/block/Block;Lorg/bukkit/inventory/ItemStack;I)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$snapshotRecipeBrewTime(Block furnace, ItemStack source, int brewingTime,
                                                 CallbackInfo ci) {
        this.paperarc$recipeBrewTime = brewingTime;
    }

    @Unique
    public int getRecipeBrewTime() {
        return this.paperarc$recipeBrewTime;
    }

    @Unique
    public void setRecipeBrewTime(int recipeBrewTime) {
        Preconditions.checkArgument(recipeBrewTime > 0, "brewing time must be greater than 0");
        this.paperarc$recipeBrewTime = recipeBrewTime;
    }

    @Unique
    public int getBrewingTime() {
        return this.getTotalBrewTime();
    }

    @Unique
    public void setBrewingTime(int brewTime) {
        Preconditions.checkArgument(brewTime > 0, "brewing time must be greater than 0");
        this.setTotalBrewTime(brewTime);
    }
}
