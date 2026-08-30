package com.ixnah.mc.paperarc.bridge;

/**
 * Bridge for Paper's {@code BeehiveBlockEntity.clearBees()} API (vanilla has no such
 * method — the {@code stored} list is private). Consumed by the beehive FieldsMixin
 * and the bukkit {@code CraftBeehiveApiMixin.clearEntities()} implementation.
 */
public interface BeehiveBlockEntityBridge {

    void paper$clearEntities();
}
