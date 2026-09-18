package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/**
 * paper 让 {@code Cat.Type} 实现 {@code org.bukkit.Keyed}（A6/X-2 第三批）。
 * vanilla 的猫变种 id 就是小写枚举名（{@code minecraft:tabby} 等），照此构造 key。
 */
@Mixin(Cat.Type.class)
public abstract class CatTypeApiMixin implements Keyed {

    @Unique
    public NamespacedKey getKey() {
        return NamespacedKey.minecraft(((Cat.Type) (Object) this).name().toLowerCase(Locale.ENGLISH));
    }
}
