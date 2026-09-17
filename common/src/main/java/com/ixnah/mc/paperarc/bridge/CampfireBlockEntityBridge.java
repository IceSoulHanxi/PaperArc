package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code CampfireBlockEntity.stopCooking}
 * supplementary field to the api mixins. Paper's patch adds the field without an
 * NMS accessor method, so the bridge methods carry the {@code paper$} prefix.
 */
public interface CampfireBlockEntityBridge {

    boolean paper$isStopCooking(int index);

    void paper$setStopCooking(int index, boolean stop);
}
