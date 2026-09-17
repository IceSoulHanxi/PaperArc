package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code Tadpole.ageLocked} supplementary
 * field to the api mixins. Paper's server patch (Missing-Entity-API.patch)
 * adds the field with no accessor methods, so the bridge methods carry the
 * {@code paper$} prefix.
 */
public interface TadpoleBridge {

    boolean paper$getAgeLocked();

    void paper$setAgeLocked(boolean ageLocked);
}
