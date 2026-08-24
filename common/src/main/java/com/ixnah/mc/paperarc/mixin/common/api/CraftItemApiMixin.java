package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ApiState;
import org.bukkit.craftbukkit.v.entity.CraftItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;

/**
 * Ports Paper additions missing from Arclight CraftBukkit {@link CraftItem}:
 * <ul>
 *   <li>Paper "Add-API-for-item-entity-health": {@code getHealth()}/{@code setHealth(int)}
 *       delegate to the vanilla {@code ItemEntity#health} field (mojmap name; private in
 *       this NMS build, so accessed via a cached reflective {@link Field}).</li>
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
    private static volatile Field PAPERARC$HEALTH_FIELD;

    @Unique
    protected final net.minecraft.world.entity.item.ItemEntity paperarc$handle() {
        return ((CraftItem) (Object) this).getHandle();
    }

    // ---- Add-API-for-item-entity-health ----

    @Unique
    public int getHealth() {
        try {
            return paperarc$healthField().getInt(this.paperarc$handle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS ItemEntity#health not accessible", e);
        }
    }

    @Unique
    public void setHealth(int health) {
        try {
            paperarc$healthField().setInt(this.paperarc$handle(), health);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS ItemEntity#health not accessible", e);
        }
    }

    @Unique
    private static Field paperarc$healthField() throws ReflectiveOperationException {
        Field f = PAPERARC$HEALTH_FIELD;
        if (f == null) {
            f = net.minecraft.world.entity.item.ItemEntity.class.getDeclaredField("health");
            f.setAccessible(true);
            PAPERARC$HEALTH_FIELD = f;
        }
        return f;
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
