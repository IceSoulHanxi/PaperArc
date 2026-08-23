package dev.paperarc.mixin.common.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.UnaryOperator;

import com.google.common.base.Preconditions;

import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.bukkit.Registry;
import org.bukkit.craftbukkit.v.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.util.RandomSourceWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.paperarc.bridge.PaperArcBridge;

/**
 * Adds Paper's ItemFactory additions to CraftItemFactory.
 *
 * Paper refs: patches/server/{Adventure,Implement-enchantWithLevels-API,
 * Add-enchantWithLevels-with-enchantment-registry-set,Implement-getI18NDisplayName,
 * ensureServerConversions-API,Create-HoverEvent-from-ItemStack-Entity}.patch.
 *
 * Mapping notes vs Paper source:
 * - {@code CraftItemStack.unwrap} is Paper-only; {@code asNMSCopy} is used instead
 *   (same safe-copy semantics for the components we touch).
 * - {@code PaperAdventure} is server-only: displayName goes through a gson round-trip
 *   (same approach as CraftObjectiveApiMixin) and {@code asHoverEvent} drops the
 *   components-patch payload (adventure DataComponentValue conversion unavailable),
 *   keeping only key + amount.
 * - {@code PaperRegistrySets.convertToNms} is Paper-infra; RegistryKeySet elements are
 *   resolved through the Bukkit registry and converted with
 *   {@code CraftEnchantment.bukkitToMinecraftHolder}.
 */
@Mixin(org.bukkit.craftbukkit.v.inventory.CraftItemFactory.class)
public abstract class CraftItemFactoryApiMixin {

    @Unique
    public net.kyori.adventure.text.event.HoverEvent<net.kyori.adventure.text.event.HoverEvent.ShowItem> asHoverEvent(
            ItemStack item, UnaryOperator<net.kyori.adventure.text.event.HoverEvent.ShowItem> op) {
        // degraded: component patch payload omitted (no PaperAdventure server-side)
        return net.kyori.adventure.text.event.HoverEvent.showItem(op.apply(
                net.kyori.adventure.text.event.HoverEvent.ShowItem.showItem(
                        item.getType().getKey(), item.getAmount())));
    }

    @Unique
    public net.kyori.adventure.text.Component displayName(ItemStack itemStack) {
        return paperarc$asAdventure(CraftItemStack.asNMSCopy(itemStack).getDisplayName());
    }

    @Unique
    public ItemStack enchantWithLevels(ItemStack itemStack, int levels,
            io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.enchantments.Enchantment> keySet,
            Random random) {
        Preconditions.checkArgument(keySet != null, "Argument 'keySet' must not be null");
        List<Holder<net.minecraft.world.item.enchantment.Enchantment>> holders = new ArrayList<>();
        for (org.bukkit.enchantments.Enchantment enchantment : keySet.resolve(Registry.ENCHANTMENT)) {
            holders.add(CraftEnchantment.bukkitToMinecraftHolder(enchantment));
        }
        return paperarc$enchantWithLevels(itemStack, levels, Optional.of(HolderSet.direct(holders)), random);
    }

    @Unique
    public ItemStack enchantWithLevels(ItemStack itemStack, int levels, boolean allowTreasure, Random random) {
        // While IN_ENCHANTING_TABLE is not logically the same as all but TREASURE, the tag is
        // defined as NON_TREASURE, which contains all enchantments not in the treasure tag
        // (comment carried over from Paper).
        net.minecraft.core.RegistryAccess registryAccess = ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess();
        Optional<HolderSet<net.minecraft.world.item.enchantment.Enchantment>> possibleEnchantments = allowTreasure
                ? Optional.empty()
                : Optional.of(registryAccess.registryOrThrow(Registries.ENCHANTMENT)
                        .getTag(EnchantmentTags.IN_ENCHANTING_TABLE)
                        .orElseThrow(() -> new IllegalStateException("Missing IN_ENCHANTING_TABLE enchantment tag")));
        return paperarc$enchantWithLevels(itemStack, levels, possibleEnchantments, random);
    }

    @Unique
    public ItemStack ensureServerConversions(ItemStack item) {
        return CraftItemStack.asCraftMirror(CraftItemStack.asNMSCopy(item));
    }

    @Unique
    public String getI18NDisplayName(ItemStack item) {
        net.minecraft.world.item.ItemStack nms = null;
        if (item instanceof CraftItemStack craftStack) {
            // CraftItemStack#getHandle() is package-private -> reflective access.
            try {
                java.lang.reflect.Method m = craftStack.getClass().getDeclaredMethod("getHandle");
                m.setAccessible(true);
                nms = (net.minecraft.world.item.ItemStack) m.invoke(craftStack);
            } catch (ReflectiveOperationException ignored) {
                // fall through to copy
            }
        }
        if (nms == null) {
            nms = CraftItemStack.asNMSCopy(item);
        }
        return nms != null
                ? Language.getInstance().getOrDefault(nms.getItem().getDescriptionId(nms))
                : null;
    }

    @Unique
    public net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(Entity entity) {
        return hoverContentOf(entity,
                org.apache.commons.lang3.StringUtils.isBlank(entity.getCustomName())
                        ? null
                        : new net.md_5.bungee.api.chat.TextComponent(entity.getCustomName()));
    }

    @Unique
    public net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(Entity entity, String customName) {
        return hoverContentOf(entity,
                org.apache.commons.lang3.StringUtils.isBlank(customName)
                        ? null
                        : new net.md_5.bungee.api.chat.TextComponent(customName));
    }

    @Unique
    public net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(Entity entity,
            net.md_5.bungee.api.chat.BaseComponent customName) {
        return new net.md_5.bungee.api.chat.hover.content.Entity(
                entity.getType().getKey().toString(),
                entity.getUniqueId().toString(),
                customName);
    }

    @Unique
    public net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(Entity entity,
            net.md_5.bungee.api.chat.BaseComponent[] customName) {
        return new net.md_5.bungee.api.chat.hover.content.Entity(
                entity.getType().getKey().toString(),
                entity.getUniqueId().toString(),
                new net.md_5.bungee.api.chat.TextComponent(customName));
    }

    @Unique
    public net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(ItemStack itemStack) {
        // verbatim Paper behaviour: BungeeCord Chat API does not support data components
        throw new UnsupportedOperationException("BungeeCord Chat API does not support data components");
    }

    @Unique
    private ItemStack paperarc$enchantWithLevels(ItemStack itemStack, int levels,
            Optional<? extends HolderSet<net.minecraft.world.item.enchantment.Enchantment>> possibleEnchantments,
            Random random) {
        Preconditions.checkArgument(itemStack != null, "Argument 'itemStack' must not be null");
        Preconditions.checkArgument(!itemStack.isEmpty(), "Argument 'itemStack' cannot be empty");
        Preconditions.checkArgument(levels > 0 && levels <= 30,
                "Argument 'levels' must be in range [1, 30] (attempted " + levels + ")");
        Preconditions.checkArgument(random != null, "Argument 'random' must not be null");
        final net.minecraft.world.item.ItemStack internalStack = CraftItemStack.asNMSCopy(itemStack);
        if (internalStack.isEnchanted()) {
            internalStack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        final net.minecraft.core.RegistryAccess registryAccess =
                ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess();
        final net.minecraft.world.item.ItemStack enchanted = EnchantmentHelper.enchantItem(
                new RandomSourceWrapper(random),
                internalStack,
                levels,
                registryAccess,
                possibleEnchantments);
        return CraftItemStack.asCraftMirror(enchanted);
    }

    @Unique
    private static net.kyori.adventure.text.Component paperarc$asAdventure(
            net.minecraft.network.chat.Component vanilla) {
        // PaperAdventure unavailable: gson round-trip (see CraftObjectiveApiMixin)
        return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson()
                .deserialize(net.minecraft.network.chat.Component.Serializer.toJson(vanilla,
                        ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess()));
    }}
