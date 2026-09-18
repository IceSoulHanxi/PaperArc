package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/**
 * paper 加在 {@code WorldCreator} 上的 key 与 keepSpawnLoaded（A6/X-2 第五批）。
 *
 * <p>{@code WorldCreator} 是插件自己 {@code new} 出来的普通 API 对象（不是包装对象），
 * 所以 {@code @Unique} 字段在它身上是稳定的。paper 把 key 做成构造参数，运行时只有
 * {@code WorldCreator(String)} 一个构造器，所以两个 static 工厂先按名字构造、再塞 key；
 * 未显式给 key 时按 paper 的 {@code getWorldKey(name)} 规则推导（主世界三个维度特殊处理）。
 *
 * <p><b>语义差异</b>：Arclight 的 {@code CraftServer#createWorld(WorldCreator)} 不读这两个
 * 值 —— 自定义 key 不会影响新世界的维度 key，{@code keepSpawnLoaded} 也不会改变出生点
 * 区块的常驻行为。往返读写是准确的，但别指望它改引擎行为；记在 docs/gaps.md。
 */
@Mixin(WorldCreator.class)
public abstract class WorldCreatorApiMixin {

    @Unique
    private NamespacedKey paperarc$key;

    @Unique
    private TriState paperarc$keepSpawnLoaded = TriState.NOT_SET;

    @Unique
    private WorldCreator paperarc$self() {
        return (WorldCreator) (Object) this;
    }

    @Unique
    public NamespacedKey key() {
        if (this.paperarc$key == null) {
            this.paperarc$key = paperarc$worldKey(this.paperarc$self().name());
        }
        return this.paperarc$key;
    }

    @Unique
    public TriState keepSpawnLoaded() {
        return this.paperarc$keepSpawnLoaded;
    }

    @Unique
    public WorldCreator keepSpawnLoaded(TriState keepSpawnLoaded) {
        com.google.common.base.Preconditions.checkArgument(keepSpawnLoaded != null,
                "keepSpawnLoaded cannot be null");
        this.paperarc$keepSpawnLoaded = keepSpawnLoaded;
        return this.paperarc$self();
    }

    @Unique
    public static WorldCreator ofKey(NamespacedKey worldKey) {
        return ofNameAndKey(worldKey.getKey(), worldKey);
    }

    @Unique
    public static WorldCreator ofNameAndKey(String levelName, NamespacedKey worldKey) {
        WorldCreator creator = new WorldCreator(levelName);
        ((WorldCreatorApiMixin) (Object) creator).paperarc$key = worldKey;
        return creator;
    }

    /** 与 paper 的 getWorldKey(name) 逐行一致。 */
    @Unique
    private static NamespacedKey paperarc$worldKey(String name) {
        String mainLevelName = Bukkit.getUnsafe().getMainLevelName();
        if (name.equals(mainLevelName)) {
            return NamespacedKey.minecraft("overworld");
        }
        if (name.equals(mainLevelName + "_nether")) {
            return NamespacedKey.minecraft("the_nether");
        }
        if (name.equals(mainLevelName + "_the_end")) {
            return NamespacedKey.minecraft("the_end");
        }
        return NamespacedKey.minecraft(name.toLowerCase(Locale.ENGLISH).replace(" ", "_"));
    }
}
