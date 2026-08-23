package dev.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code ServerLevel#hasEntityMoveEvent}
 * flag, used by the EntityMoveEvent support mixins.
 */
public interface ServerLevelMoveEventBridge {

    boolean paperarc$hasEntityMoveEvent();
}
