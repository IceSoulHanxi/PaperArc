package com.ixnah.mc.paperarc.bridge;

/**
 * Thread-local coordination between {@code FireBlockPrimeMixin} (reports
 * TNTPrimeEvent with PrimeReason.FIRE from {@code FireBlock#checkBurnOut})
 * and {@code TntBlockPrimeMixin} (chokepoint on the private three-arg
 * {@code TntBlock#explode}).
 *
 * <p>Vanilla funnels FIRE priming through the two-arg static explode into the
 * three-arg form, so without this guard the same TNT block would fire a
 * second, bogus REDSTONE event right after the FIRE one. The fire mixin marks
 * the thread before the prime proceeds; the chokepoint mixes consume the mark
 * and skip their own event for that call.
 */
public final class TNTPrimeState {

    private static final ThreadLocal<Boolean> FIRE_PRIME = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private TNTPrimeState() {
    }

    /** Marks that a FIRE-prime has already reported an event on this thread. */
    public static void setFirePrime(boolean value) {
        FIRE_PRIME.set(value);
    }

    /** Returns whether a FIRE-prime mark is set and clears it immediately. */
    public static boolean takeFirePrime() {
        boolean value = FIRE_PRIME.get();
        FIRE_PRIME.set(false);
        return value;
    }
}