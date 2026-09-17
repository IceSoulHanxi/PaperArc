package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code AbstractSkeleton.shouldBurnInDay}
 * supplementary field to the api mixins. Paper's patch provides NMS access
 * methods {@code shouldBurnInDay()} / {@code setShouldBurnInDay(boolean)}, so
 * the bridge methods keep Paper's names.
 */
public interface AbstractSkeletonBridge {

    boolean shouldBurnInDay();

    void setShouldBurnInDay(boolean shouldBurnInDay);
}
