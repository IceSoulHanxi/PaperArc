package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.entity.HumanEntity;
import java.util.Collection;
import java.util.Set;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.HumanEntity} (generated).
 * Adds 14 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.HumanEntity", remap = false)
public interface HumanEntityIfaceMixin {

    @Unique
    public abstract org.bukkit.inventory.InventoryView openAnvil(org.bukkit.Location p0, boolean p1);

    @Unique
    public abstract org.bukkit.inventory.InventoryView openCartographyTable(org.bukkit.Location p0, boolean p1);

    @Unique
    public abstract org.bukkit.inventory.InventoryView openGrindstone(org.bukkit.Location p0, boolean p1);

    @Unique
    public abstract org.bukkit.inventory.InventoryView openLoom(org.bukkit.Location p0, boolean p1);

    @Unique
    public abstract org.bukkit.inventory.InventoryView openSmithingTable(org.bukkit.Location p0, boolean p1);

    @Unique
    public abstract org.bukkit.inventory.InventoryView openStonecutter(org.bukkit.Location p0, boolean p1);

    @Unique
    public abstract void setHurtDirection(float p0);

    @Unique
    public abstract boolean isDeeplySleeping();

    @Unique
    public abstract org.bukkit.Location getPotentialBedLocation();

    @Unique
    public abstract org.bukkit.entity.FishHook getFishHook();

    @Unique
    public abstract org.bukkit.entity.Entity releaseLeftShoulderEntity();

    @Unique
    public abstract org.bukkit.entity.Entity releaseRightShoulderEntity();

    @Unique
    public abstract void openSign(org.bukkit.block.Sign p0, org.bukkit.block.sign.Side p1);

    @Unique
    public default void openSign(Sign sign) {
        HumanEntity self = (HumanEntity) this;
        self.openSign(sign, Side.FRONT);
    }
}
