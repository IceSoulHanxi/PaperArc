package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.Namespaced;
import com.ixnah.mc.paperarc.bridge.craft.CraftMetaItemAdventureBridge;
import com.ixnah.mc.paperarc.bridge.craft.PaperarcAdventureModeKeys;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.AdventureModePredicate;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Paper 的 CanPlaceOn / CanDestroy 落盘（gaps.md G.1）。
 *
 * <p>之前这两组键只存在 {@code CraftMetaItem} 的内存字段里：能设能读，但既不写进物品、
 * 也不影响冒险模式的放置/破坏判定 —— 典型的"存了个值没人读"。
 *
 * <p>1.21.1 的落点是数据组件 {@code minecraft:can_place_on} / {@code minecraft:can_break}。
 * 锚点选 {@code CraftItemStack} 的两个静态方法而不是 {@code CraftMetaItem#applyToItem}：
 * 后者的形参类型 {@code CraftMetaItem$Applicator} 是**包私有**的，mixin 源码在别的包里
 * 连写都写不出来；{@code setItemMeta(ItemStack, ItemMeta)} / {@code getItemMeta(ItemStack)}
 * 全是 public，且是 meta ↔ 物品的唯一汇聚点。
 *
 * <p>两条注解都**不能**写 {@code remap = false}（docs/mixin-conventions.md bm）：选择器的
 * 描述符里带 NMS 的 {@code ItemStack}，在 Fabric 上要被重映射成 {@code class_1799}；
 * 第一版写了 {@code remap = false}，当场 {@code could not find any targets matching
 * setItemMeta(Lnet/minecraft/world/item/ItemStack;…)}。
 */
@Mixin(CraftItemStack.class)
public abstract class CraftItemStackAdventureModeMixin {

    @Inject(method = "setItemMeta(Lnet/minecraft/world/item/ItemStack;Lorg/bukkit/inventory/meta/ItemMeta;)Z",
            at = @At("RETURN"))
    private static void paperarc$applyAdventureModeKeys(net.minecraft.world.item.ItemStack stack, ItemMeta meta,
                                                        CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue()) || stack == null
                || !(meta instanceof CraftMetaItemAdventureBridge bridge)) {
            return;
        }
        paperarc$apply(stack, DataComponents.CAN_PLACE_ON, bridge.paperarc$placeableKeys());
        paperarc$apply(stack, DataComponents.CAN_BREAK, bridge.paperarc$destroyableKeys());
    }

    @ModifyReturnValue(method = "getItemMeta(Lnet/minecraft/world/item/ItemStack;)Lorg/bukkit/inventory/meta/ItemMeta;",
            at = @At("RETURN"))
    private static ItemMeta paperarc$readAdventureModeKeys(ItemMeta meta,
                                                           net.minecraft.world.item.ItemStack stack) {
        if (stack == null || !(meta instanceof CraftMetaItemAdventureBridge bridge)) {
            return meta;
        }
        bridge.paperarc$setPlaceableKeys(PaperarcAdventureModeKeys.fromPredicate(
                stack.get(DataComponents.CAN_PLACE_ON)));
        bridge.paperarc$setDestroyableKeys(PaperarcAdventureModeKeys.fromPredicate(
                stack.get(DataComponents.CAN_BREAK)));
        return meta;
    }

    private static void paperarc$apply(net.minecraft.world.item.ItemStack stack,
                                       net.minecraft.core.component.DataComponentType<AdventureModePredicate> type,
                                       Set<Namespaced> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        AdventureModePredicate predicate = PaperarcAdventureModeKeys.toPredicate(keys);
        if (predicate != null) {
            stack.set(type, predicate);
        }
    }
}
