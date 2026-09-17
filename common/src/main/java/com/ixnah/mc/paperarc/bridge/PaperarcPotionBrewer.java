package com.ixnah.mc.paperarc.bridge;

import io.papermc.paper.potion.PotionMix;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 最小 PotionBrewer：Paper 的自定义配方放内存副表，vanilla 酿造注册表不动。
 * 1.20.1 分支的同名类委托 {@code CraftPotionBrewer}，Arclight 1.21.1 运行时没有该类，
 * 因此这里保留 main 原有的纯内存实现。
 *
 * <p>放在 {@code bridge} 而不是 mixin 包：合并后的 {@code CraftServer} 字节码要直接
 * 引用它，而 Mixin 禁止目标类引用 mixin 包内的类。内嵌在 mixin 里还会让合并后的
 * {@code CraftServer} 与内嵌类的 InnerClasses 属性互相矛盾，插件一调
 * {@code getPotionBrewer()} 就 IncompatibleClassChangeError（B1 三端实测）。</p>
 */
public final class PaperarcPotionBrewer implements PotionBrewer {

    private static final List<PotionMix> MIXES = new ArrayList<>();

    @Override
    public void addPotionMix(PotionMix mix) {
        synchronized (MIXES) {
            MIXES.removeIf(existing -> existing.getKey().equals(mix.getKey()));
            MIXES.add(mix);
        }
    }

    @Override
    public void removePotionMix(NamespacedKey key) {
        synchronized (MIXES) {
            MIXES.removeIf(existing -> existing.getKey().equals(key));
        }
    }

    @Override
    public void resetPotionMixes() {
        synchronized (MIXES) {
            MIXES.clear();
        }
    }

    @Override
    public Collection<PotionEffect> getEffects(PotionType type, boolean upgraded, boolean extended) {
        // upgraded/extended variants are not tracked without the NMS mix
        // registry; the base effect set is returned for every variant.
        return type.getPotionEffects();
    }
}

