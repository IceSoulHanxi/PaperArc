package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code Zombie.shouldBurnInDay}
 * supplementary field to the api mixins. Paper's server patch
 * (Add-more-Zombie-API.patch) adds the field plus NMS accessor methods
 * {@code shouldBurnInDay()} / {@code setShouldBurnInDay(boolean)}. Since a
 * getter already exists in Paper, the bridge getter uses that name; only a
 * setter path carries the {@code paper$} prefix is not needed here, but the
 * field is also read directly by {@code isSunSensitive()} in Paper, so the
 * bridge keeps both directions under the Paper names.
 */
public interface ZombieBridge {

    boolean shouldBurnInDay();

    void setShouldBurnInDay(boolean shouldBurnInDay);
}
