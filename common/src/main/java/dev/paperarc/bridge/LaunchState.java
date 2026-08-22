package dev.paperarc.bridge;

/**
 * Thread-local state shared between the multiple injection points that together
 * implement Paper's PlayerLaunchProjectileEvent semantics inside a single
 * {@code use()} / {@code useOn()} invocation (cancel / shouldConsume flags).
 *
 * Flags are always cleared by the terminal {@code @ModifyReturnValue} handler;
 * a stale flag can only leak if an exception aborts the method mid-way, which
 * is accepted for now.
 */
public final class LaunchState {

    private static final int CANCELLED = 1;
    private static final int NO_CONSUME = 2;
    private static final ThreadLocal<Integer> FLAGS = ThreadLocal.withInitial(() -> 0);

    private LaunchState() {
    }

    public static void cancelled(boolean value) {
        FLAGS.set(setBit(FLAGS.get(), CANCELLED, value));
    }

    public static void noConsume(boolean value) {
        FLAGS.set(setBit(FLAGS.get(), NO_CONSUME, value));
    }

    public static boolean isCancelled() {
        return (FLAGS.get() & CANCELLED) != 0;
    }

    public static boolean isNoConsume() {
        return (FLAGS.get() & NO_CONSUME) != 0;
    }

    /** Returns whether the launch was cancelled and clears both flags. */
    public static boolean takeCancelled() {
        int state = FLAGS.get();
        FLAGS.set(0);
        return (state & CANCELLED) != 0;
    }

    private static int setBit(int state, int bit, boolean value) {
        return value ? (state | bit) : (state & ~bit);
    }
}
