package io.izzel.arclight.common.bridge.core.world.item.crafting;

/**
 * 编译期桩：Arclight 给 {@code RecipeHolder} 加的桥接口，
 * 把 NMS 配方转成对应的 Bukkit {@code Recipe}（运行时由 Arclight 自己的 mixin 提供实现）。
 * 桩不进产物，见 {@code common/build.gradle} 的 {@code arclightStub} 源码集。
 */
public interface RecipeHolderBridge {

    org.bukkit.inventory.Recipe bridge$toBukkitRecipe();
}
