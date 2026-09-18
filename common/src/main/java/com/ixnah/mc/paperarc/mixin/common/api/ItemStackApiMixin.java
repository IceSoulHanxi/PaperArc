package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * paper 加在 {@code org.bukkit.inventory.ItemStack} 上的方法与两个 implements
 * （{@code HoverEventSource}、adventure {@code Translatable}），A6/X-2 第四批共 31 条。
 *
 * <p>逐条照 paper 的 {@code ItemStack.java}：数量/lore/flag 一类走
 * {@code getItemMeta()}+{@code setItemMeta()}，其余委托 {@code ItemFactory} 或
 * {@code Bukkit.getUnsafe()}。两个 static（{@code empty()}/{@code deserializeBytes()}）
 * 依赖 {@code markSyntheticStaticApi} 打 ACC_SYNTHETIC 才能合并进目标类，
 * 所以本文件列在 {@code gradle/mixin-static-allowlist.txt}。
 *
 * <p>注意 {@code CraftItemStack} 是子类：它 override 了 {@code getItemMeta/setItemMeta}，
 * 这里的方法体都经这两个虚方法走，子类自动生效，不需要再给 CraftItemStack 单独补。
 */
@Mixin(ItemStack.class)
public abstract class ItemStackApiMixin implements HoverEventSource<HoverEvent.ShowItem>, Translatable {

    @Unique
    private ItemStack paperarc$self() {
        return (ItemStack) (Object) this;
    }

    @Unique
    private ItemMeta paperarc$meta(String action) {
        ItemMeta meta = this.paperarc$self().getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("Cannot " + action + " on " + this.paperarc$self().getType());
        }
        return meta;
    }

    // ---- 空栈 / 数量 ----

    /** paper 的 {@code new ItemStack()}（type=AIR、amount=0）；那个构造器是包私有，这里等价构造。 */
    @Unique
    public static ItemStack empty() {
        ItemStack empty = new ItemStack(Material.AIR);
        empty.setAmount(0);
        return empty;
    }

    @Unique
    public boolean isEmpty() {
        ItemStack self = this.paperarc$self();
        return self.getType().isAir() || self.getAmount() <= 0;
    }

    @Unique
    public ItemStack asOne() {
        return this.paperarc$self().asQuantity(1);
    }

    @Unique
    public ItemStack asQuantity(int qty) {
        ItemStack clone = this.paperarc$self().clone();
        clone.setAmount(qty);
        return clone;
    }

    @Unique
    public ItemStack add() {
        return this.paperarc$self().add(1);
    }

    @Unique
    public ItemStack add(int qty) {
        ItemStack self = this.paperarc$self();
        self.setAmount(Math.min(self.getMaxStackSize(), self.getAmount() + qty));
        return self;
    }

    @Unique
    public ItemStack subtract() {
        return this.paperarc$self().subtract(1);
    }

    @Unique
    public ItemStack subtract(int qty) {
        ItemStack self = this.paperarc$self();
        self.setAmount(Math.max(0, self.getAmount() - qty));
        return self;
    }

    // ---- meta 编辑 ----

    @Unique
    public boolean editMeta(Consumer<? super ItemMeta> consumer) {
        return this.paperarc$self().editMeta(ItemMeta.class, consumer);
    }

    @Unique
    @SuppressWarnings("unchecked")
    public <M extends ItemMeta> boolean editMeta(Class<M> metaClass, Consumer<? super M> consumer) {
        ItemStack self = this.paperarc$self();
        ItemMeta meta = self.getItemMeta();
        if (metaClass.isInstance(meta)) {
            consumer.accept((M) meta);
            self.setItemMeta(meta);
            return true;
        }
        return false;
    }

    // ---- lore / flag ----

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
        ItemMeta meta = this.paperarc$meta("set lore");
        meta.setLore(lore);
        this.paperarc$self().setItemMeta(meta);
    }

    @Unique
    public void lore(List<? extends Component> lore) {
        ItemMeta meta = this.paperarc$meta("set lore");
        meta.lore(lore);
        this.paperarc$self().setItemMeta(meta);
    }

    @Unique
    public void addItemFlags(ItemFlag... itemFlags) {
        ItemMeta meta = this.paperarc$meta("add flags");
        meta.addItemFlags(itemFlags);
        this.paperarc$self().setItemMeta(meta);
    }

    @Unique
    public void removeItemFlags(ItemFlag... itemFlags) {
        ItemMeta meta = this.paperarc$meta("remove flags");
        meta.removeItemFlags(itemFlags);
        this.paperarc$self().setItemMeta(meta);
    }

    @Unique
    public Set<ItemFlag> getItemFlags() {
        ItemStack self = this.paperarc$self();
        if (!self.hasItemMeta()) {
            return Collections.emptySet();
        }
        ItemMeta meta = self.getItemMeta();
        return meta == null ? Collections.emptySet() : meta.getItemFlags();
    }

    @Unique
    public boolean hasItemFlag(ItemFlag flag) {
        ItemStack self = this.paperarc$self();
        if (!self.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = self.getItemMeta();
        return meta != null && meta.hasItemFlag(flag);
    }

    // ---- adventure ----

    @Unique
    public Component displayName() {
        return Bukkit.getServer().getItemFactory().displayName(this.paperarc$self());
    }

    @Unique
    public HoverEvent<HoverEvent.ShowItem> asHoverEvent(UnaryOperator<HoverEvent.ShowItem> op) {
        return Bukkit.getServer().getItemFactory().asHoverEvent(this.paperarc$self(), op);
    }

    @Unique
    public String translationKey() {
        return Bukkit.getUnsafe().getTranslationKey(this.paperarc$self());
    }

    // ---- 其它 ----

    @Unique
    public ItemStack ensureServerConversions() {
        return Bukkit.getServer().getItemFactory().ensureServerConversions(this.paperarc$self());
    }

    @Unique
    public String getI18NDisplayName() {
        return Bukkit.getServer().getItemFactory().getI18NDisplayName(this.paperarc$self());
    }

    @Unique
    public ItemStack enchantWithLevels(int levels, boolean allowTreasure, Random random) {
        return Bukkit.getServer().getItemFactory()
                .enchantWithLevels(this.paperarc$self(), levels, allowTreasure, random);
    }

    @Unique
    public byte[] serializeAsBytes() {
        return Bukkit.getUnsafe().serializeItem(this.paperarc$self());
    }

    @Unique
    public static ItemStack deserializeBytes(byte[] bytes) {
        return Bukkit.getUnsafe().deserializeItem(bytes);
    }

    @Unique
    public io.papermc.paper.inventory.ItemRarity getRarity() {
        return Bukkit.getUnsafe().getItemStackRarity(this.paperarc$self());
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
    public ItemStack damage(int amount, org.bukkit.entity.LivingEntity livingEntity) {
        return livingEntity.damageItemStack(this.paperarc$self(), amount);
    }

    /**
     * paper 的做法是基类先 {@code ensureServerConversions()}、再由 {@code CraftItemStack}
     * 的重写版读自己的 NMS handle。这里直接走 {@code CraftItemStack.asNMSCopy}
     * 一步到位 —— 少一个只为转发存在的子类 mixin，取值完全一样
     * （普通 ItemStack 与 CraftItemStack 都适用）。
     */
    @Unique
    public int getMaxItemUseDuration() {
        ItemStack self = this.paperarc$self();
        Material type = self.getType();
        if (type == Material.AIR || !type.isItem()) {
            return 0;
        }
        net.minecraft.world.item.ItemStack nms =
                org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(self);
        return nms == null ? 0 : nms.getUseDuration();
    }
}
