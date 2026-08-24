package com.ixnah.mc.paperarc.bridge;

import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Side-channel storage for paper-api state that vanilla NMS classes have no
 * field for (e.g. Beacon effect range, ArmorStand marker flags added by Paper).
 *
 * Keys are the Craft or NMS objects themselves (weakly referenced so entries die
 * with the object); values are small maps keyed by a string slot name.
 * Thread-safety: ConcurrentHashMap per object, matching typical Bukkit access
 * patterns (main thread writes, any thread reads).
 */
public final class ApiState {

    private static final WeakHashMap<Object, ConcurrentHashMap<String, Object>> STATE = new WeakHashMap<>();

    private ApiState() {
    }

    public static void put(Object owner, String key, Object value) {
        map(owner).put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object owner, String key, T defaultValue) {
        ConcurrentHashMap<String, Object> m = STATE.get(owner);
        if (m == null) return defaultValue;
        Object v = m.get(key);
        return v == null ? defaultValue : (T) v;
    }

    public static boolean has(Object owner, String key) {
        ConcurrentHashMap<String, Object> m = STATE.get(owner);
        return m != null && m.containsKey(key);
    }

    public static void remove(Object owner, String key) {
        ConcurrentHashMap<String, Object> m = STATE.get(owner);
        if (m != null) m.remove(key);
    }

    private static synchronized ConcurrentHashMap<String, Object> map(Object owner) {
        return STATE.computeIfAbsent(owner, k -> new ConcurrentHashMap<>());
    }
}
