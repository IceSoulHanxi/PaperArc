package io.izzel.arclight.common.bridge.core.item.crafting;

import org.bukkit.inventory.Recipe;

/**
 * Arclight {@code IRecipeBridge} 的**编译期桩**（只声明我们用到的那一个方法）。
 *
 * <p>运行时 NMS {@code Recipe} 由 Arclight 的 {@code IRecipeMixin} 实现这个接口，
 * {@code bridge$toBukkitRecipe()} 就是 CraftBukkit 打在 NMS {@code Recipe} 上的
 * {@code toBukkitRecipe()}（Forge 的 NMS 上没有这个方法，所以 Arclight 用 bridge 接口转发）。
 * {@code BlockCookEvent#getRecipe()} 需要把 NMS 配方转成 bukkit 的 {@code CookingRecipe}。
 *
 * <p>这个源码集不进产物，见同源码集 {@code EntityClassLookup} 的注释。
 */
public interface IRecipeBridge {

    Recipe bridge$toBukkitRecipe();
}
