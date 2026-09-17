package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code Entity.fixedPose}, {@code freezeLocked}
 * and origin supplementary fields to the api mixins. Paper's patches add
 * {@code fixedPose}/{@code freezeLocked} as bare fields (no NMS accessor), so
 * those bridge methods carry the {@code paper$} prefix. The origin API
 * (Entity-Origin-API.patch) adds NMS accessor methods
 * {@code setOrigin(Location)} / {@code getOriginVector()} / {@code getOriginWorld()},
 * so those bridge methods use the Paper names without a prefix.
 */
public interface EntityBridge {

    boolean paper$fixedPose();

    void paper$setFixedPose(boolean fixedPose);

    boolean paper$freezeLocked();

    void paper$setFreezeLocked(boolean freezeLocked);

    void setOrigin(org.bukkit.Location location);

    org.bukkit.util.Vector getOriginVector();

    java.util.UUID getOriginWorld();
}
