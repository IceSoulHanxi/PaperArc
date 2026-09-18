package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcItemStacks;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import io.papermc.paper.inventory.tooltip.TooltipContext;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.registry.set.RegistryKeySet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * paper 加在 {@code org.bukkit.inventory.ItemStack} 上的方法（checklist §1.10 am，第 ③ 批）。
 *
 * <p>方法体照抄 paper-api 反编译结果。paper 版 ItemStack 有一个私有的
 * {@code craftDelegate} 字段（指向 CraftItemStack），运行时的 Bukkit ItemStack 没有，
 * 所以走 craftDelegate 的四个方法改写成运行时等价物：
 * {@code isEmpty()} → {@code PaperarcItemStacks}；
 * {@code withType} → {@code clone()} 后 {@code setType}（Bukkit 的 setType 本身就会做
 * meta 转换）；{@code getPersistentDataContainer()} → 转调 ItemMeta 的同名方法
 * （{@code PersistentDataContainerIfaceMixin} 已让它返回 View）；
 * {@code getMaxItemUseDuration} → {@code CraftItemStack.asNMSCopy} 后问 NMS。</p>
 *
 * <p>7 个 {@code static} 方法写成 {@code private static} + {@code @Widen}：Mixin 拒绝
 * 合并非 private 的 static 方法，放宽由 {@code bridge.WidenPostProcessor} 在 postApply
 * 阶段做（B5，见 docs/mixin-conventions.md 的「@Widen」一节）。</p>
 */
@Mixin(ItemStack.class)
public abstract class ItemStackApiMixin {

    @Unique
    @Widen(because = "paper-api: public static ItemStack of(...)")
    private static ItemStack of(Material type) {
        return new ItemStack(type);
    }

    @Unique
    @Widen(because = "paper-api: public static ItemStack of(...)")
    private static ItemStack of(Material type, int amount) {
        return new ItemStack(type, amount);
    }

    @Unique
    @Widen(because = "paper-api: public static ItemStack deserializeBytes(...)")
    private static ItemStack deserializeBytes(byte[] bytes) {
        return Bukkit.getUnsafe().deserializeItem(bytes);
    }

    @Unique
    @Widen(because = "paper-api: public static byte[] serializeItemsAsBytes(...)")
    private static byte[] serializeItemsAsBytes(Collection<ItemStack> items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            DataOutputStream output = new DataOutputStream(outputStream);
            output.writeByte(1);
            output.writeInt(items.size());
            for (ItemStack item : items) {
                if (item != null && !PaperarcItemStacks.isEmpty(item)) {
                    byte[] itemBytes = item.serializeAsBytes();
                    output.writeInt(itemBytes.length);
                    output.write(itemBytes);
                } else {
                    output.writeInt(0);
                }
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error while writing itemstack", e);
        }
    }

    @Unique
    @Widen(because = "paper-api: public static byte[] serializeItemsAsBytes(...)")
    private static byte[] serializeItemsAsBytes(ItemStack[] items) {
        return serializeItemsAsBytes(Arrays.asList(items));
    }

    @Unique
    @Widen(because = "paper-api: public static ItemStack[] deserializeItemsFromBytes(...)")
    private static ItemStack[] deserializeItemsFromBytes(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            DataInputStream input = new DataInputStream(inputStream);
            byte version = input.readByte();
            if (version != 1) {
                throw new IllegalArgumentException("Unsupported version or bad data: " + version);
            }
            int count = input.readInt();
            ItemStack[] items = new ItemStack[count];
            for (int i = 0; i < count; i++) {
                int length = input.readInt();
                if (length == 0) {
                    items[i] = ItemStack.empty();
                } else {
                    byte[] itemBytes = new byte[length];
                    input.readFully(itemBytes);
                    items[i] = ItemStack.deserializeBytes(itemBytes);
                }
            }
            return items;
        } catch (IOException e) {
            throw new RuntimeException("Error while reading itemstack", e);
        }
    }

    @Unique
    @Widen(because = "paper-api: public static ItemStack empty(...)")
    private static ItemStack empty() {
        return Bukkit.getUnsafe().createEmptyStack();
    }

    @Unique
    private ItemStack paperarc$self() {
        return (ItemStack) (Object) this;
    }



    @Unique
    public PersistentDataContainerView getPersistentDataContainer() {
        ItemMeta meta = this.paperarc$self().getItemMeta();
        return meta == null ? null : meta.getPersistentDataContainer();
    }

    @Unique
    public ItemStack withType(Material type) {
        ItemStack copy = this.paperarc$self().clone();
        copy.setType(type);
        return copy;
    }

    @Unique
    public boolean editMeta(Consumer<? super ItemMeta> consumer) {
        return this.editMeta(ItemMeta.class, consumer);
    }

    @Unique
    public <M extends ItemMeta> boolean editMeta(Class<M> metaClass, Consumer<? super M> consumer) {
        ItemStack self = this.paperarc$self();
        ItemMeta meta = self.getItemMeta();
        if (!metaClass.isInstance(meta)) {
            return false;
        }
        consumer.accept(metaClass.cast(meta));
        self.setItemMeta(meta);
        return true;
    }

    @Unique
    public ItemStack enchantWithLevels(int levels, boolean allowTreasure, Random random) {
        return Bukkit.getServer().getItemFactory()
                .enchantWithLevels(this.paperarc$self(), levels, allowTreasure, random);
    }

    @Unique
    public ItemStack enchantWithLevels(int levels, RegistryKeySet<Enchantment> keySet, Random random) {
        return Bukkit.getItemFactory().enchantWithLevels(this.paperarc$self(), levels, keySet, random);
    }

    @Unique
    public HoverEvent<HoverEvent.ShowItem> asHoverEvent(UnaryOperator<HoverEvent.ShowItem> op) {
        return Bukkit.getServer().getItemFactory().asHoverEvent(this.paperarc$self(), op);
    }

    @Unique
    public Component displayName() {
        return Bukkit.getServer().getItemFactory().displayName(this.paperarc$self());
    }

    @Unique
    public ItemStack ensureServerConversions() {
        return Bukkit.getServer().getItemFactory().ensureServerConversions(this.paperarc$self());
    }


    @Unique
    public byte[] serializeAsBytes() {
        return Bukkit.getUnsafe().serializeItem(this.paperarc$self());
    }




    @Unique
    public String getI18NDisplayName() {
        return Bukkit.getServer().getItemFactory().getI18NDisplayName(this.paperarc$self());
    }

    @Unique
    public int getMaxItemUseDuration() {
        return this.getMaxItemUseDuration(null);
    }

    @Unique
    public int getMaxItemUseDuration(LivingEntity entity) {
        net.minecraft.world.item.ItemStack nms =
                org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(this.paperarc$self());
        net.minecraft.world.entity.LivingEntity handle = entity == null
                ? null
                : ((org.bukkit.craftbukkit.v.entity.CraftLivingEntity) entity).getHandle();
        return nms.getUseDuration(handle);
    }

    @Unique
    public ItemStack asOne() {
        return this.asQuantity(1);
    }

    @Unique
    public ItemStack asQuantity(int qty) {
        ItemStack clone = this.paperarc$self().clone();
        clone.setAmount(qty);
        return clone;
    }

    @Unique
    public ItemStack add() {
        return this.add(1);
    }

    @Unique
    public ItemStack add(int qty) {
        ItemStack self = this.paperarc$self();
        self.setAmount(Math.min(self.getMaxStackSize(), self.getAmount() + qty));
        return self;
    }

    @Unique
    public ItemStack subtract() {
        return this.subtract(1);
    }

    @Unique
    public ItemStack subtract(int qty) {
        ItemStack self = this.paperarc$self();
        self.setAmount(Math.max(0, self.getAmount() - qty));
        return self;
    }

    @Unique
    public List<String> getLore() {
        ItemStack self = this.paperarc$self();
        if (!self.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = self.getItemMeta();
        return meta.hasLore() ? meta.getLore() : null;
    }

    @Unique
    public List<Component> lore() {
        ItemStack self = this.paperarc$self();
        if (!self.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = self.getItemMeta();
        return meta.hasLore() ? meta.lore() : null;
    }

    @Unique
    public void setLore(List<String> lore) {
        ItemStack self = this.paperarc$self();
        ItemMeta meta = self.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("Cannot set lore on " + self.getType());
        }
        meta.setLore(lore);
        self.setItemMeta(meta);
    }

    @Unique
    public void lore(List<? extends Component> lore) {
        ItemStack self = this.paperarc$self();
        ItemMeta meta = self.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("Cannot set lore on " + self.getType());
        }
        meta.lore(lore);
        self.setItemMeta(meta);
    }

    @Unique
    public void addItemFlags(ItemFlag... itemFlags) {
        ItemStack self = this.paperarc$self();
        ItemMeta meta = self.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("Cannot add flags on " + self.getType());
        }
        meta.addItemFlags(itemFlags);
        self.setItemMeta(meta);
    }

    @Unique
    public void removeItemFlags(ItemFlag... itemFlags) {
        ItemStack self = this.paperarc$self();
        ItemMeta meta = self.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("Cannot remove flags on " + self.getType());
        }
        meta.removeItemFlags(itemFlags);
        self.setItemMeta(meta);
    }

    @Unique
    public Set<ItemFlag> getItemFlags() {
        ItemMeta meta = this.paperarc$self().getItemMeta();
        return meta == null ? Collections.emptySet() : meta.getItemFlags();
    }

    @Unique
    public boolean hasItemFlag(ItemFlag flag) {
        ItemMeta meta = this.paperarc$self().getItemMeta();
        return meta != null && meta.hasItemFlag(flag);
    }

    @Unique
    public String translationKey() {
        return Bukkit.getUnsafe().getTranslationKey(this.paperarc$self());
    }

    /**
     * paper 直接 {@code getItemMeta().getRarity()}；Arclight 的 CraftMetaItem 只在
     * 物品带 rarity 数据组件时才有值，没有就抛
     * {@code IllegalStateException: We don't have rarity!}（探针 P23 实测）。
     * 这里没设过就回落到 NMS 的物品默认稀有度，与 paper 的取值一致。
     */
    @Unique
    public io.papermc.paper.inventory.ItemRarity getRarity() {
        ItemMeta meta = this.paperarc$self().getItemMeta();
        if (meta != null && meta.hasRarity()) {
            return io.papermc.paper.inventory.ItemRarity.valueOf(meta.getRarity().name());
        }
        net.minecraft.world.item.ItemStack nms =
                org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(this.paperarc$self());
        return io.papermc.paper.inventory.ItemRarity.valueOf(nms.getRarity().name());
    }

    @Unique
    public boolean isRepairableBy(ItemStack repairMaterial) {
        return Bukkit.getUnsafe().isValidRepairItemStack(this.paperarc$self(), repairMaterial);
    }

    @Unique
    public boolean canRepair(ItemStack toBeRepaired) {
        return Bukkit.getUnsafe().isValidRepairItemStack(toBeRepaired, this.paperarc$self());
    }

    @Unique
    public ItemStack damage(int amount, LivingEntity livingEntity) {
        return livingEntity.damageItemStack(this.paperarc$self(), amount);
    }


    @Unique
    public boolean isEmpty() {
        return PaperarcItemStacks.isEmpty(this.paperarc$self());
    }

    @Unique
    public List<Component> computeTooltipLines(TooltipContext tooltipContext, Player player) {
        return Bukkit.getUnsafe().computeTooltipLines(this.paperarc$self(), tooltipContext, player);
    }
}
