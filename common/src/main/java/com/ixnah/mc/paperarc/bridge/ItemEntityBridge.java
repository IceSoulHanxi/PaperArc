package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code ItemEntity.canMobPickup}
 * supplementary field to the api mixins. Paper's patch adds the field without an
 * NMS accessor method, so the bridge methods carry the {@code paper$} prefix.
 *
 * <p>{@code canPlayerPickup}/{@code willAge} are <em>not</em> NMS fields in
 * Paper either — they map onto the vanilla {@code pickupDelay}/{@code age}
 * fields and need no injection.</p>
 */
public interface ItemEntityBridge {

    boolean paper$canMobPickup();

    void paper$setCanMobPickup(boolean canMobPickup);
}
