package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ItemEntityBridge;
import org.bukkit.craftbukkit.v.entity.CraftItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Ports Paper additions missing from Arclight CraftBukkit {@link CraftItem}:
 * <ul>
 *   <li>Paper "Add-API-for-item-entity-health": {@code getHealth()}/{@code setHealth(int)}
 *       delegate to the vanilla {@code ItemEntity#health} field, widened via AT
 *       (f_31987_) and accessed directly — no reflection.</li>
 *   <li>Paper "Item-canEntityPickup" ({@code canMobPickup}): this flag is a
 *       Paper-added {@code ItemEntity} field injected into the NMS entity by
 *       {@code ItemEntityFieldsMixin} and reached through
 *       {@link com.ixnah.mc.paperarc.bridge.ItemEntityBridge}, defaulting to
 *       {@code true} like a freshly spawned vanilla item.</li>
 *   <li>Paper "Item-no-age-no-player-pickup" ({@code canPlayerPickup},
 *       {@code shouldAge}): Paper has <em>no</em> NMS fields for these — it maps
 *       them onto the vanilla {@code pickupDelay}/{@code age} fields. Both are
 *       private, widened via AT (f_31986_ / f_31985_) and read/written directly,
 *       mirroring Paper's CraftItem logic (INFINITE_PICKUP_DELAY=32767,
 *       INFINITE_LIFETIME=-32768).</li>
 * </ul>
 */
@Mixin(CraftItem.class)
public abstract class CraftItemApiMixin {

    @Unique
    protected final net.minecraft.world.entity.item.ItemEntity paperarc$handle() {
        // 1.20.1 CraftEntity#getHandle() returns Entity; the actual ItemEntity is
        // reachable via the single-arg constructor (CraftServer, ItemEntity).
        return (net.minecraft.world.entity.item.ItemEntity) ((CraftItem) (Object) this).getHandle();
    }

    // ---- Add-API-for-item-entity-health ----

    @Unique
    public int getHealth() {
        return this.paperarc$handle().health;
    }

    @Unique
    public void setHealth(int health) {
        this.paperarc$handle().health = health;
    }

    // ---- Item-canEntityPickup ----

    @Unique
    public boolean canMobPickup() {
        return ((ItemEntityBridge) this.paperarc$handle()).paper$canMobPickup();
    }

    @Unique
    public void setCanMobPickup(boolean canMobPickup) {
        ((ItemEntityBridge) this.paperarc$handle()).paper$setCanMobPickup(canMobPickup);
    }

    // ---- Item-no-age-no-player-pickup ----

    @Unique
    public boolean canPlayerPickup() {
        return this.paperarc$handle().pickupDelay != 32767; // INFINITE_PICKUP_DELAY
    }

    @Unique
    public void setCanPlayerPickup(boolean canPlayerPickup) {
        this.paperarc$handle().pickupDelay = canPlayerPickup ? 0 : 32767; // INFINITE_PICKUP_DELAY
    }

    @Unique
    public boolean willAge() {
        return this.paperarc$handle().age != -32768; // INFINITE_LIFETIME
    }

    @Unique
    public void setWillAge(boolean willAge) {
        this.paperarc$handle().age = willAge ? 0 : -32768; // INFINITE_LIFETIME
    }
}
