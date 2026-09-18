package com.ixnah.mc.paperarc.bridge.craft;

import com.destroystokyo.paper.Namespaced;
import com.destroystokyo.paper.NamespacedTag;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.level.block.Block;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Paper 的 CanPlaceOn / CanDestroy 键 ↔ 1.21.1 的 {@code AdventureModePredicate} 数据组件
 * （gaps.md G.1）。
 *
 * <p>1.20.1 走的是 {@code unhandledTags} 里的 {@code CanPlaceOn}/{@code CanDestroy} 字符串列表；
 * 1.21.1 vanilla 换成了数据组件 {@code minecraft:can_place_on} / {@code minecraft:can_break}，
 * 值是 {@code AdventureModePredicate}（一串 {@code BlockPredicate}）。
 * 键里既可能是具体方块（{@code NamespacedKey}），也可能是方块标签（{@code NamespacedTag}），
 * 分别对应 {@code BlockPredicate.Builder#of(Block...)} 与 {@code of(TagKey)}。
 *
 * <p>不做的事：Paper 的 {@code showInTooltip} 在这套 API 里没有对应开关，
 * 与 CraftBukkit 一样写 {@code true}（= 客户端照常显示"可以放在…"那几行）。
 *
 * <p>读回要遍历 {@code AdventureModePredicate.predicates}，1.21.1 没有公开 getter，
 * 由 {@code paperarc.accesswidener} 放开（Paper 那边是补丁 publicize 的）。
 */
public final class PaperarcAdventureModeKeys {

    private PaperarcAdventureModeKeys() {
    }

    public static AdventureModePredicate toPredicate(Set<Namespaced> keys) {
        List<BlockPredicate> predicates = new ArrayList<>(keys.size());
        for (Namespaced key : keys) {
            ResourceLocation id = ResourceLocation.tryBuild(key.getNamespace(), key.getKey());
            if (id == null) {
                continue;
            }
            if (key instanceof NamespacedTag) {
                predicates.add(BlockPredicate.Builder.block()
                        .of(TagKey.create(Registries.BLOCK, id)).build());
            } else {
                Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(id);
                if (block != null) {
                    predicates.add(BlockPredicate.Builder.block().of(block).build());
                }
            }
        }
        return predicates.isEmpty() ? null : new AdventureModePredicate(predicates, true);
    }

    public static Set<Namespaced> fromPredicate(AdventureModePredicate predicate) {
        Set<Namespaced> keys = new LinkedHashSet<>();
        if (predicate == null) {
            return keys;
        }
        for (BlockPredicate block : predicate.predicates) {
            HolderSet<Block> blocks = block.blocks().orElse(null);
            if (blocks == null) {
                continue;
            }
            java.util.Optional<TagKey<Block>> tag = blocks.unwrapKey();
            if (tag.isPresent()) {
                ResourceLocation id = tag.get().location();
                keys.add(new NamespacedTag(id.getNamespace(), id.getPath()));
                continue;
            }
            for (Holder<Block> holder : blocks) {
                holder.unwrapKey().ifPresent(resourceKey -> {
                    ResourceLocation id = resourceKey.location();
                    keys.add(new NamespacedKey(id.getNamespace(), id.getPath()));
                });
            }
        }
        return keys;
    }
}
