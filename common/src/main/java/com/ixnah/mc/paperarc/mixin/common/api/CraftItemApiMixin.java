package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ApiState;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Ports Paper additions missing from Arclight CraftBukkit {@link CraftItem}:
 * <ul>
 *   <li>Paper "Add-API-for-item-entity-health": {@code getHealth()}/{@code setHealth(int)}
 *       delegate to the vanilla {@code ItemEntity#health} field, widened via AT
 *       (f_31987_) and accessed directly — no reflection.</li>
 *   <li>Paper "Item-canEntityPickup" ({@code canMobPickup}), "Item-no-age-no-player-pickup"
 *       ({@code canPlayerPickup}, {@code shouldAge}): these flags are Paper-added
 *       {@code ItemEntity} fields with no counterpart in the spigot-based NMS build, so
 *       they are stored in {@link ApiState} keyed by the NMS entity ("side-map"), defaulting
 *       to {@code true} like a freshly spawned vanilla item. Only the API surface is
 *       provided here; wiring vanilla pickup/age behaviour to the flags remains a bridge
 *       concern.</li>
 * </ul>
 */
@Mixin(CraftItem.class)
public abstract class CraftItemApiMixin {

    private static final String PAPERARC$CAN_MOB_PICKUP = "canMobPickup";
    private static final String PAPERARC$CAN_PLAYER_PICKUP = "canPlayerPickup";
    private static final String PAPERARC$SHOULD_AGE = "shouldAge";

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
        return ApiState.get(this.paperarc$handle(), PAPERARC$CAN_MOB_PICKUP, Boolean.TRUE);
    }

    @Unique
    public void setCanMobPickup(boolean canMobPickup) {
        ApiState.put(this.paperarc$handle(), PAPERARC$CAN_MOB_PICKUP, canMobPickup);
    }

    // ---- Item-no-age-no-player-pickup ----

    @Unique
    public boolean canPlayerPickup() {
        return ApiState.get(this.paperarc$handle(), PAPERARC$CAN_PLAYER_PICKUP, Boolean.TRUE);
    }

    @Unique
    public void setCanPlayerPickup(boolean canPlayerPickup) {
        ApiState.put(this.paperarc$handle(), PAPERARC$CAN_PLAYER_PICKUP, canPlayerPickup);
    }

    @Unique
    public boolean willAge() {
        return ApiState.get(this.paperarc$handle(), PAPERARC$SHOULD_AGE, Boolean.TRUE);
    }

    @Unique
    public void setWillAge(boolean willAge) {
        ApiState.put(this.paperarc$handle(), PAPERARC$SHOULD_AGE, willAge);
    }
}
