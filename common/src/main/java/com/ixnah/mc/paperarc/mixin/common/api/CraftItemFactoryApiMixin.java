package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Random;
import java.util.function.UnaryOperator;

import com.google.common.base.Preconditions;

import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.locale.Language;

import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.RandomSourceWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;

/**
 * Adds Paper's ItemFactory additions to CraftItemFactory.
 *
 * Paper refs: patches/server/{Adventure,Implement-enchantWithLevels-API,
 * Implement-getI18NDisplayName,ensureServerConversions-API,
 * Create-HoverEvent-from-ItemStack-Entity}.patch.
 *
 * Mapping notes vs Paper source:
 * - {@code CraftItemStack.unwrap} is Paper-only; {@code asNMSCopy} is used instead.
 * - {@code PaperAdventure} is server-only: displayName goes through a gson round-trip
 *   and {@code asHoverEvent} drops the components-patch payload, keeping key + amount.
 * - enchantWithLevels follows the 1.20.1 Paper implementation (no data components).
 */
@Mixin(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemFactory.class)
public abstract class CraftItemFactoryApiMixin {

    /**
     * Accessor for the NMS stack behind a CraftItemStack: Paper's package-private
     * {@code getHandle()} is tried first; current Arclight builds omit the method,
     * so the equivalent package-private {@code handle} field is read instead.
     * Null -> callers fall back to {@link CraftItemStack#asNMSCopy(ItemStack)}.
     */
    @Unique
    private static final MethodHandle PAPERARC$GET_HANDLE = paperarc$buildGetHandleHandle();

    @Unique
    private static MethodHandle paperarc$buildGetHandleHandle() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            return MethodHandles.privateLookupIn(CraftItemStack.class, lookup)
                    .findVirtual(CraftItemStack.class, "getHandle",
                            MethodType.methodType(net.minecraft.world.item.ItemStack.class));
        } catch (ReflectiveOperationException ignored) {
            // this Arclight build has no CraftItemStack#getHandle(); use the field
        }
        try {
            return MethodHandles.privateLookupIn(CraftItemStack.class, lookup)
                    .findGetter(CraftItemStack.class, "handle", net.minecraft.world.item.ItemStack.class);
        } catch (ReflectiveOperationException e) {
            return null; // caller degrades to asNMSCopy
        }
    }

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
    public ItemStack enchantWithLevels(ItemStack itemStack, int levels, boolean allowTreasure, Random random) {
        Preconditions.checkArgument(itemStack != null, "Argument 'itemStack' must not be null");
        Preconditions.checkArgument(itemStack.getType() != org.bukkit.Material.AIR, "Argument 'itemStack' must not be of type AIR");
        Preconditions.checkArgument(itemStack.getAmount() > 0, "Argument 'itemStack' amount must be greater than 0");
        Preconditions.checkArgument(levels > 0 && levels <= 30, "Argument 'levels' must be in range [1, 30] (attempted " + levels + ")");
        Preconditions.checkArgument(random != null, "Argument 'random' must not be null");
        final net.minecraft.world.item.ItemStack internalStack = CraftItemStack.asNMSCopy(itemStack);
        if (internalStack.getTag() != null) {
            internalStack.getTag().remove(net.minecraft.world.item.ItemStack.TAG_ENCH);
        }
        final net.minecraft.world.item.ItemStack enchanted =
                net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(
                        new RandomSourceWrapper(random), internalStack, levels, allowTreasure);
        return CraftItemStack.asCraftMirror(enchanted);
    }

    @Unique
    public ItemStack ensureServerConversions(ItemStack item) {
        return CraftItemStack.asCraftMirror(CraftItemStack.asNMSCopy(item));
    }

    @Unique
    public String getI18NDisplayName(ItemStack item) {
        net.minecraft.world.item.ItemStack nms = null;
        if (item instanceof CraftItemStack craftStack && PAPERARC$GET_HANDLE != null) {
            // CraftItemStack#getHandle()/handle are package-private -> MethodHandle access.
            try {
                nms = (net.minecraft.world.item.ItemStack) PAPERARC$GET_HANDLE.invokeExact(craftStack);
            } catch (Throwable ignored) {
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
    private static net.kyori.adventure.text.Component paperarc$asAdventure(
            net.minecraft.network.chat.Component vanilla) {
        // PaperAdventure unavailable: gson round-trip
        return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson()
                .deserialize(net.minecraft.network.chat.Component.Serializer.toJson(vanilla));
    }
}
