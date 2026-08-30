package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code Slime.canWander} supplementary field
 * to the api mixins. Paper's server patch
 * (Slime-Pathfinder-Events.patch) adds the field plus NMS accessor methods
 * {@code canWander()} / {@code setWander(boolean)}, so the bridge methods use
 * those Paper names without a prefix.
 */
public interface SlimeBridge {

    boolean canWander();

    void setWander(boolean canWander);
}
