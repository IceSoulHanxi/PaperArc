package com.ixnah.mc.paperarc.bridge;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic runtime class injector for paper-api types that are absent from
 * Arclight's spigot-api baseline.
 *
 * <p>Which classes to inject is declared in a JSON config embedded next to the
 * class bytes (see {@link #CONFIG}): each entry names a class using its
 * original jar path, and the corresponding {@code .class} bytes are stored
 * under {@code /META-INF/paperarc/runtime/<internal name>.class} (produced by
 * the {@code embedRuntimeClasses} Gradle task). {@link #inject()} reads that
 * config, hooks the mixin bytecode provider so the embedded bytes are served
 * when mixin resolves those type names, and {@code defineClass}-es the bytes
 * into every reachable classloader.</p>
 *
 * <p>Why not ship them as {@code .class} files in {@code org/bukkit/**}: the
 * module system rejects a mod jar exporting a package already exported by the
 * arclight mod (run30 {@code ResolutionException}). The bytes therefore live
 * under a neutral resource path and are injected at runtime.</p>
 *
 * <p>Why {@code defineClass} alone is not enough: mixin resolves descriptor
 * types by <em>bytecode</em> ({@code getClassNode} → {@code transformerLoader}
 * {@code buildTransformedClassNodeFor} / TCCL {@code getResource(name+".class")}),
 * not by {@code Class.forName}. A {@code defineClass}-ed class has no
 * {@code .class} resource, so mixin would still fail with
 * {@code ClassMetadataNotFoundException}. {@link #hookTransformerLoader(Map)}
 * therefore wraps the modlauncher {@code ITransformerLoader} owned by the mixin
 * service's bytecode provider and serves the embedded bytes for any configured
 * class name (dotted, as {@code buildTransformedClassNodeFor} receives).</p>
 *
 * <p><b>Timing — two phases.</b> {@code defineClass} never runs {@code <clinit>},
 * but the JVM <em>must</em> resolve a class's direct superclass and superinterfaces
 * while <em>defining</em> it (JVMS §5.3.5); no "do not initialise" flag avoids that.
 * Defining a type whose supertype chain reaches an {@code org.bukkit.*} runtime
 * interface therefore loads that interface — and if this happens from
 * {@link RuntimeClassConfigPlugin#onLoad}, sibling mixin configs in the same phase
 * are still preparing, so their iface mixins fail with
 * {@code MixinTargetAlreadyLoadedException}. Hence:</p>
 * <ul>
 *   <li>{@link #hookAndDefineSafe()} — called from {@code onLoad}. Always hooks the
 *       bytecode provider (side-effect free) and only defines classes whose whole
 *       supertype chain consists of {@code java.*} types or other injected types
 *       (enums, {@code World$ChunkLoadCallback}, …).</li>
 *   <li>{@link #defineDeferred()} — called from the mod constructor, which is after
 *       every mixin config has been prepared and still before Arclight's bukkit layer
 *       (and thus the Craft* classes) is initialised. Defining the remaining classes
 *       here loads their bukkit supertypes through the normal transformer, so the
 *       iface mixins still apply.</li>
 * </ul>
 * <p>Deferred defines must <em>not</em> be done from {@code preApply}/
 * {@code shouldApplyMixin}: those run inside mixin transformation, where a nested
 * class load hits MixinProcessor's re-entrance handling.</p>
 *
 * <p>Method descriptors referencing {@code org.bukkit} types are harmless — only
 * direct supertypes are resolved at define time.</p>
 */
public final class RuntimeClassInjector {

    /** JSON config listing the classes to inject (original jar path as the key). */
    private static final String CONFIG = "/META-INF/paperarc/runtime/injections.json";

    private static final String LOG = "/tmp/paperarc-injector.log";
    private static final Pattern CLASS_NAME = Pattern.compile("\"([a-zA-Z0-9_$.]+)\"");

    private static volatile boolean hookDone;
    private static volatile boolean deferredDone;

    /** Config parsed once by {@link #hookAndDefineSafe()} and reused by {@link #defineDeferred()}. */
    private static volatile Map<String, byte[]> configured;

    private RuntimeClassInjector() {
    }

    /**
     * Phase 1 (mixin config {@code onLoad}): hook the bytecode provider for every
     * configured class and define the ones that cannot drag a bukkit runtime type in.
     * Idempotent.
     */
    public static synchronized void hookAndDefineSafe() {
        if (hookDone) {
            return;
        }
        Map<String, byte[]> classes = configuredClasses();
        if (classes == null || classes.isEmpty()) {
            hookDone = true;
            return;
        }
        hookTransformerLoader(classes);
        Map<String, byte[]> safe = new LinkedHashMap<>();
        Map<String, byte[]> deferred = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
            (supertypesAreSafe(entry.getKey(), classes, new LinkedHashSet<>()) ? safe : deferred)
                    .put(entry.getKey(), entry.getValue());
        }
        hookDone = true;
        trace("[PaperArc] RuntimeClassInjector: phase1 safe=" + safe.keySet()
                + " deferred=" + deferred.keySet());
        defineAll("phase1", safe);
    }

    /**
     * Phase 2 (mod constructor): define the classes whose supertype chain reaches a
     * bukkit runtime type, now that every mixin config has been prepared. Idempotent.
     */
    public static synchronized void defineDeferred() {
        if (deferredDone) {
            return;
        }
        Map<String, byte[]> classes = configuredClasses();
        deferredDone = true;
        if (classes == null || classes.isEmpty()) {
            return;
        }
        Map<String, byte[]> deferred = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
            if (!supertypesAreSafe(entry.getKey(), classes, new LinkedHashSet<>())) {
                deferred.put(entry.getKey(), entry.getValue());
            }
        }
        if (!hookDone) {
            // onLoad never ran (config plugin missing?) — fall back to a full injection
            hookTransformerLoader(classes);
            hookDone = true;
            defineAll("phase2-fallback", classes);
            return;
        }
        defineAll("phase2", deferred);
    }

    /** defineClass {@code classes} into every reachable loader that does not have them yet. */
    private static void defineAll(String phase, Map<String, byte[]> classes) {
        if (classes.isEmpty()) {
            trace("[PaperArc] RuntimeClassInjector: " + phase + " nothing to define");
            return;
        }
        boolean any = false;
        StringBuilder sb = new StringBuilder();
        for (ClassLoader loader : candidateLoaders()) {
            if (loader == null) {
                continue;
            }
            sb.append(describe(loader)).append(" ");
            for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
                String name = entry.getKey();
                try {
                    Class.forName(name, false, loader);
                    continue; // already present in this loader
                } catch (ClassNotFoundException expected) {
                }
                try {
                    io.izzel.arclight.api.Unsafe.defineClass(name, entry.getValue(), 0, entry.getValue().length, loader, null);
                    Class.forName(name, false, loader);
                    trace("[PaperArc] RuntimeClassInjector: injected " + name + " into " + describe(loader));
                    any = true;
                } catch (Throwable t) {
                    trace("[PaperArc] RuntimeClassInjector: failed for " + name + " in " + describe(loader) + ": " + t);
                }
            }
        }
        trace("[PaperArc] RuntimeClassInjector: " + phase + " classes=" + classes.keySet()
                + " candidates=[" + sb + "] done, injected=" + any);
    }

    /**
     * True when {@code name}'s whole supertype chain only contains {@code java.*} types
     * and other configured (injectable) types, i.e. defining it cannot pull an
     * {@code org.bukkit.*} runtime class in. Read straight off the embedded bytes.
     */
    private static boolean supertypesAreSafe(String name, Map<String, byte[]> classes, Set<String> seen) {
        if (!seen.add(name)) {
            return true; // cycle guard; already being validated higher up the stack
        }
        byte[] bytes = classes.get(name);
        if (bytes == null) {
            return false;
        }
        org.objectweb.asm.ClassReader reader;
        try {
            reader = new org.objectweb.asm.ClassReader(bytes);
        } catch (Throwable t) {
            trace("[PaperArc] RuntimeClassInjector: cannot read " + name + ", treating as deferred: " + t);
            return false;
        }
        java.util.List<String> supers = new java.util.ArrayList<>();
        if (reader.getSuperName() != null) {
            supers.add(reader.getSuperName().replace('/', '.'));
        }
        String[] interfaces = reader.getInterfaces();
        if (interfaces != null) {
            for (String itf : interfaces) {
                supers.add(itf.replace('/', '.'));
            }
        }
        for (String supertype : supers) {
            if (supertype.startsWith("java.")) {
                continue;
            }
            if (classes.containsKey(supertype)) {
                if (!supertypesAreSafe(supertype, classes, seen)) {
                    return false;
                }
                continue;
            }
            return false; // resolved from the runtime -> must be deferred
        }
        return true;
    }

    /** Parses (and caches) the embedded config. */
    private static Map<String, byte[]> configuredClasses() {
        Map<String, byte[]> cached = configured;
        if (cached != null) {
            return cached;
        }
        try {
            cached = readConfiguredClasses();
        } catch (Throwable t) {
            trace("[PaperArc] RuntimeClassInjector: read config failed: " + t);
            cached = java.util.Collections.emptyMap();
        }
        if (cached == null || cached.isEmpty()) {
            trace("[PaperArc] RuntimeClassInjector: no classes configured in " + CONFIG);
            cached = java.util.Collections.emptyMap();
        }
        configured = cached;
        return cached;
    }

    /** Parses the JSON config and loads each configured class's bytes. */
    private static Map<String, byte[]> readConfiguredClasses() throws Exception {
        String json;
        try (InputStream in = RuntimeClassInjector.class.getResourceAsStream(CONFIG)) {
            if (in == null) {
                return null;
            }
            json = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        Map<String, byte[]> map = new LinkedHashMap<>();
        Matcher matcher = CLASS_NAME.matcher(json);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (name.startsWith("org.") || name.startsWith("io.papermc") || name.startsWith("com.destroystokyo")) {
                String internal = name.replace('.', '/') + ".class";
                String resource = "/META-INF/paperarc/runtime/" + internal;
                try (InputStream in = RuntimeClassInjector.class.getResourceAsStream(resource)) {
                    if (in == null) {
                        trace("[PaperArc] RuntimeClassInjector: bytes not found for " + name + " at " + resource);
                        continue;
                    }
                    map.put(name, in.readAllBytes());
                }
            }
        }
        return map;
    }

    /** Collects the mod loader's whole parent chain plus TCCL, system loader and module layers. */
    private static Set<ClassLoader> candidateLoaders() {
        Set<ClassLoader> loaders = new LinkedHashSet<>();
        ClassLoader cur = RuntimeClassInjector.class.getClassLoader();
        while (cur != null) {
            loaders.add(cur);
            cur = cur.getParent();
        }
        Thread t = Thread.currentThread();
        ClassLoader tccl = t.getContextClassLoader();
        if (tccl != null) {
            loaders.add(tccl);
            cur = tccl.getParent();
            while (cur != null) {
                loaders.add(cur);
                cur = cur.getParent();
            }
        }
        ClassLoader sys = ClassLoader.getSystemClassLoader();
        if (sys != null) {
            loaders.add(sys);
        }
        collectModuleLoaders(loaders);
        return loaders;
    }

    /** Adds classloaders of all modules in every reachable layer (game + boot). */
    private static void collectModuleLoaders(Set<ClassLoader> loaders) {
        java.util.ArrayDeque<ModuleLayer> queue = new java.util.ArrayDeque<>();
        ModuleLayer own = RuntimeClassInjector.class.getModule().getLayer();
        if (own != null) {
            queue.add(own);
        }
        queue.add(ModuleLayer.boot());
        while (!queue.isEmpty()) {
            ModuleLayer layer = queue.poll();
            try {
                for (Module module : layer.modules()) {
                    ClassLoader cl = module.getClassLoader();
                    if (cl != null) {
                        loaders.add(cl);
                    }
                }
            } catch (Throwable ignored) {
            }
            queue.addAll(layer.parents());
        }
    }

    private static String describe(ClassLoader loader) {
        return loader.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(loader));
    }

    /**
     * Wraps the modlauncher ITransformerLoader owned by the mixin service's
     * bytecode provider (MixinLaunchPlugin / MixinLaunchPluginLegacy) with a
     * dynamic proxy that serves embedded bytes for any configured class name,
     * delegating everything else to the original loader.
     */
    private static void hookTransformerLoader(Map<String, byte[]> classes) {
        try {
            org.spongepowered.asm.service.IMixinService service =
                    org.spongepowered.asm.service.MixinService.getService();
            Object provider = service.getBytecodeProvider();
            java.lang.reflect.Field field = findField(provider.getClass(), "transformerLoader");
            if (field == null) {
                trace("[PaperArc] RuntimeClassInjector: transformerLoader not found on "
                        + provider.getClass().getName() + " nor any superclass");
                return;
            }
            field.setAccessible(true);
            Object original = field.get(provider);
            if (original == null) {
                trace("[PaperArc] RuntimeClassInjector: transformerLoader is null; skipped");
                return;
            }
            ClassLoader providerCl = provider.getClass().getClassLoader();
            Class<?> iface = Class.forName(
                    "cpw.mods.modlauncher.serviceapi.ILaunchPluginService$ITransformerLoader",
                    false, providerCl);
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(providerCl,
                    new Class<?>[]{iface}, (p, method, args) -> {
                        if (method.getName().equals("buildTransformedClassNodeFor")
                                && args != null && args.length == 1) {
                            byte[] embedded = classes.get(args[0]);
                            if (embedded != null) {
                                return embedded;
                            }
                        }
                        return method.invoke(original, args);
                    });
            field.set(provider, proxy);
            trace("[PaperArc] RuntimeClassInjector: hooked transformerLoader; "
                    + classes.keySet() + " served via bytecode provider");
        } catch (Throwable t) {
            trace("[PaperArc] RuntimeClassInjector: hook transformerLoader failed: " + t);
        }
    }

    /** Walks the class hierarchy looking for a declared field. */
    private static java.lang.reflect.Field findField(Class<?> type, String name) {
        Class<?> cur = type;
        while (cur != null) {
            try {
                return cur.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                cur = cur.getSuperclass();
            }
        }
        return null;
    }

    /** Traces to stderr AND a file so it is captured regardless of FML redirects. */
    private static void trace(String msg) {
        System.err.println(msg);
        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get(LOG),
                    (java.time.LocalDateTime.now() + " " + msg + "\n").getBytes(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }
}
