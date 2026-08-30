package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code Phantom.shouldBurnInDay} and
 * {@code spawningEntity} supplementary fields to the api mixins.
 *
 * <p>Paper's patch provides NMS access methods {@code shouldBurnInDay()} /
 * {@code setShouldBurnInDay(boolean)} and {@code setSpawningEntity(UUID)}, so
 * those bridge methods keep Paper's names. There is no getter for
 * {@code spawningEntity} in Paper's patch, so the getter carries the
 * {@code paper$} prefix.</p>
 */
public interface PhantomBridge {

    boolean shouldBurnInDay();

    void setShouldBurnInDay(boolean shouldBurnInDay);

    void setSpawningEntity(java.util.UUID entity);

    java.util.UUID paper$getSpawningEntity();
}
