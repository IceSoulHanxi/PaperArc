package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code ArmorStand.canMove}/{@code canTick}/
 * {@code canTickSetByAPI} supplementary fields to the api mixins. Paper's
 * server patch adds these as public NMS fields with no accessor methods, so the
 * bridge methods carry the {@code paper$} prefix.
 */
public interface ArmorStandBridge {

    boolean paper$canMove();

    void paper$setCanMove(boolean canMove);

    boolean paper$canTick();

    void paper$setCanTick(boolean canTick);

    boolean paper$canTickSetByAPI();

    void paper$setCanTickSetByAPI(boolean canTickSetByAPI);
}
