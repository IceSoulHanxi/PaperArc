package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code WitherBoss.canPortal} supplementary
 * field to the api mixins. Paper's server patch (Missing-Entity-API.patch)
 * adds the field plus a NMS setter {@code setCanTravelThroughPortals(boolean)}
 * but no getter, so the setter uses the Paper name while the getter carries
 * the {@code paper$} prefix.
 */
public interface WitherBossBridge {

    boolean paper$canPortal();

    void setCanTravelThroughPortals(boolean canPortal);
}
