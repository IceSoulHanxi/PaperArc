package dev.paperarc.bridge;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;

/**
 * ThreadLocal hand-off of the Paper spawn cause from caller mixins
 * (SetSpawnCommand, respawn anchor, bed path) into ServerPlayerSetSpawnMixin.
 * Lives outside mixin classes because Sponge Mixin only allows private
 * added statics.
 */
public final class SpawnCauseSupport {

    private static final ThreadLocal<PlayerSetSpawnEvent.Cause> CAUSE = new ThreadLocal<>();

    private SpawnCauseSupport() {
    }

    public static void push(PlayerSetSpawnEvent.Cause cause) {
        CAUSE.set(cause);
    }

    public static void clear() {
        CAUSE.remove();
    }

    public static PlayerSetSpawnEvent.Cause peek() {
        return CAUSE.get();
    }
}
