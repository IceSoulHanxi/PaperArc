package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code AbstractFurnaceBlockEntity
 * .cookSpeedMultiplier} supplementary field to the api mixins. Paper's patch
 * adds the field without an NMS accessor method, so the bridge methods carry
 * the {@code paper$} prefix.
 */
public interface AbstractFurnaceBlockEntityBridge {

    double paper$getCookSpeedMultiplier();

    void paper$setCookSpeedMultiplier(double multiplier);
}
