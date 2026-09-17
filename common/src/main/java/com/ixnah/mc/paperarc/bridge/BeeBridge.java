package com.ixnah.mc.paperarc.bridge;

import net.kyori.adventure.util.TriState;

/**
 * Duck interface exposing Paper's {@code Bee.rollingOverride} supplementary
 * field to the api mixins. Paper's patch adds the field without an NMS accessor
 * method, so the bridge methods carry the {@code paper$} prefix.
 */
public interface BeeBridge {

    TriState paper$getRollingOverride();

    void paper$setRollingOverride(TriState rolling);
}
