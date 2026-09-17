package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.TagKey;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.kyori.adventure.key.Key;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockType;
import org.bukkit.block.banner.PatternType;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.loot.LootTables;
import org.bukkit.map.MapCursor;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.Registry} 的 paper default 方法体（照抄 paper-api）。
 *
 * <p>paper 的 {@code Registry<T extends Keyed>} 带类型参数，mixin 接口上没有对应物；
 * {@code T} 的擦除就是 {@code Keyed}，方法描述符与 paper 完全一致，所以正文里一律
 * 写成 {@code Keyed}（插件侧的泛型检查在它自己的编译期已经做过了）。
 */
@Mixin(targets = "org.bukkit.Registry", remap = false)
public interface RegistryIfaceMixin {

    @Unique
    public default Keyed get(Key key) {
        Registry self = (Registry) this;
        Keyed keyed;

        if (key instanceof NamespacedKey nsKey) {
            keyed = self.get(nsKey);
        } else {
            keyed = self.get(new NamespacedKey(key.namespace(), key.value()));
        }

        return keyed;
    }

    @Unique
    public default Keyed get(TypedKey<? extends Keyed> typedKey) {
        Registry self = (Registry) this;
        return self.get(typedKey.key());
    }

    @Unique
    public default Keyed getOrThrow(Key key) {
        Registry self = (Registry) this;
        Keyed value = self.get(key);

        if (value == null) {
            String s = String.valueOf(key);

            throw new NoSuchElementException("No value for " + s + " in " + String.valueOf(self));
        } else {
            return value;
        }
    }

    @Unique
    public default Keyed getOrThrow(TypedKey<? extends Keyed> key) {
        Registry self = (Registry) this;
        Keyed value = self.get(key);

        if (value == null) {
            String s = String.valueOf(key);

            throw new NoSuchElementException("No value for " + s + " in " + String.valueOf(self));
        } else {
            return value;
        }
    }

    @Unique
    public default NamespacedKey getKeyOrThrow(Keyed value) {
        Registry self = (Registry) this;
        Preconditions.checkArgument(value != null, "value cannot be null");
        NamespacedKey key = self.getKey(value);

        if (key == null) {
            String s = String.valueOf(value);

            throw new NoSuchElementException(s + " has no key in " + String.valueOf(self));
        } else {
            return key;
        }
    }

    @Unique
    public default NamespacedKey getKey(Keyed value) {
        Preconditions.checkArgument(value != null, "value cannot be null");
        return value instanceof Keyed ? value.getKey() : null;
    }

    @Unique
    public default boolean hasTag(TagKey<Keyed> key) {
        Registry self = (Registry) this;
        throw new UnsupportedOperationException(String.valueOf(self) + " doesn't have tags");
    }

    @Unique
    public default io.papermc.paper.registry.tag.Tag<Keyed> getTag(TagKey<Keyed> key) {
        Registry self = (Registry) this;
        throw new UnsupportedOperationException(String.valueOf(self) + " doesn't have tags");
    }
}
