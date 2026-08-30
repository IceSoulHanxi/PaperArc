package com.ixnah.mc.paperarc.bridge;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Injects the runtime-missing {@code org.bukkit.entity.ExperienceOrb$SpawnReason}
 * enum into the game classloader(s).
 *
 * <p>The enum type is added by paper-api 1.20.1 but is absent from Arclight's
 * spigot-api baseline. It cannot be shipped as a {@code .class} file inside the
 * mod jar under {@code org/bukkit/entity/} — the module system would reject the
 * jar at launch (paperarc and arclight exporting the same package → run30
 * ResolutionException). Instead the class bytes are embedded at build time
 * under a neutral resource path and {@code defineClass}-ed at runtime.</p>
 *
 * <p>Loader targeting: the mod's own class is loaded by a per-mod
 * {@code ModuleClassLoader} (a <em>child</em> of the game
 * {@code TransformingClassLoader}). A class defined only into the mod loader is
 * invisible to the TransformingClassLoader that Mixin uses to resolve types for
 * NMS/mojmap mixins (parent cannot see child classes). Therefore {@link #inject()}
 * walks the whole parent chain up to the bootstrap loader and defines the class
 * into <em>every</em> loader, plus the thread-context and system loaders. Any
 * loader that already has the type is skipped.</p>
 *
 * <p>Timing: {@link #inject()} is invoked both from the mod constructor and —
 * more importantly — from {@link SpawnReasonConfigPlugin#onLoad}, which runs
 * when the mixin config is loaded, before <em>any</em> mixin in that config is
 * applied (including the bukkit-side iface/api mixins that reference the enum
 * in their method signatures). The NMS-side {@code ExperienceOrbFieldsMixin}
 * stores the reason as an {@code int} ordinal and never references the enum
 * type, so Minecraft's Bootstrap passes even before the injection runs.</p>
 *
 * <p>Class definition uses {@code io.izzel.arclight.api.Unsafe#defineClass},
 * which resolves {@code ClassLoader#defineClass0/defineClassInternal} through a
 * {@code MethodHandles.Lookup} and therefore works on JDK 17 without
 * {@code --add-opens} (plain {@code sun.misc.Unsafe#defineClass} was removed in
 * JDK 11).</p>
 */
public final class SpawnReasonInjector {

    private static final String TARGET = "org.bukkit.entity.ExperienceOrb$SpawnReason";
    private static final String RESOURCE = "/META-INF/paperarc/runtime/spawnreason.bin";

    private static volatile boolean done;

    private SpawnReasonInjector() {
    }

    public static synchronized void inject() {
        if (done) {
            return;
        }
        byte[] bytes = null;
        try {
            bytes = readResource();
        } catch (Throwable t) {
            trace("[PaperArc] SpawnReasonInjector: read resource failed: " + t);
            return;
        }
        if (bytes == null) {
            trace("[PaperArc] SpawnReasonInjector: resource " + RESOURCE + " not found; getSpawnReason stays unavailable");
            return;
        }
        // Mixin resolves types by BYTECODE (getClassNode -> transformerLoader
        // buildTransformedClassNodeFor / TCCL getResource(name+'.class')), not by
        // Class.forName. defineClass'd classes have no .class resource, so mixin
        // still fails with ClassMetadataNotFoundException. Hook the transformer
        // loader so it serves the embedded bytes for the missing enum.
        hookTransformerLoader(bytes);
        boolean any = false;
        StringBuilder sb = new StringBuilder();
        for (ClassLoader loader : candidateLoaders()) {
            if (loader == null) {
                continue;
            }
            sb.append(describe(loader)).append(" ");
            try {
                // Already present in this loader → skip.
                Class.forName(TARGET, false, loader);
                continue;
            } catch (ClassNotFoundException expected) {
            }
            try {
                io.izzel.arclight.api.Unsafe.defineClass(TARGET, bytes, 0, bytes.length, loader, null);
                Class<?> defined = Class.forName(TARGET, false, loader);
                trace("[PaperArc] SpawnReasonInjector: injected " + defined.getName() + " into " + describe(loader));
                any = true;
            } catch (Throwable t) {
                trace("[PaperArc] SpawnReasonInjector: failed for loader " + describe(loader) + ": " + t);
            }
        }
        done = true;
        trace("[PaperArc] SpawnReasonInjector: candidates=[" + sb + "] done, injected=" + any);
    }

    /** Collects the mod loader's whole parent chain plus TCCL and system loader. */
    private static Set<ClassLoader> candidateLoaders() {
        Set<ClassLoader> loaders = new LinkedHashSet<>();
        ClassLoader start = SpawnReasonInjector.class.getClassLoader();
        ClassLoader cur = start;
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
        // Mixin resolves the type with the loader of the TARGET class
        // (org.bukkit.entity.ExperienceOrb / CraftExperienceOrb), which live in
        // the arclight mod jar and are loaded by that mod's ModuleClassLoader.
        // A per-mod ModuleClassLoader only sees its own module resources, it does
        // NOT delegate to dynamically-defined classes in TransformingClassLoader,
        // so classes injected into the parent chain are invisible to it. Instead
        // we must also define the class into every module layer whose modules
        // own the org.bukkit.entity package.
        collectBukkitPackageLoaders(loaders);
        return loaders;
    }

    /** Adds the classloaders of all modules that own the bukkit package. */
    private static void collectBukkitPackageLoaders(Set<ClassLoader> loaders) {
        java.util.ArrayDeque<ModuleLayer> queue = new java.util.ArrayDeque<>();
        // The mod layer (GAME layer) is a CHILD of the boot layer: it is created
        // after boot and lists boot as its parent, so walking boot+parents would
        // miss it. Start from the layer that contains this very mod class, which
        // is the game layer that also holds the arclight module, then fall back
        // to the boot layer and walk parents.
        ModuleLayer own = SpawnReasonInjector.class.getModule().getLayer();
        trace("[PaperArc] SpawnReasonInjector: own layer=" + own
                + " hasBukkit=" + (own != null && own.modules().stream().anyMatch(
                        m -> m.getPackages().contains("org.bukkit.entity"))));
        if (own != null) {
            queue.add(own);
        }
        queue.add(ModuleLayer.boot());
        while (!queue.isEmpty()) {
            ModuleLayer layer = queue.poll();
            try {
                for (Module module : layer.modules()) {
                    ClassLoader cl = module.getClassLoader();
                    if (module.getPackages().contains("org.bukkit.entity")) {
                        trace("[PaperArc] SpawnReasonInjector: bukkit package owned by module "
                                + module.getName() + " cl=" + cl);
                    }
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
     * bytecode provider (MixinLaunchPluginLegacy) with a dynamic proxy that
     * serves the embedded SpawnReason bytes for its internal name, delegating
     * everything else to the original loader.
     */
    private static void hookTransformerLoader(byte[] bytes) {
        try {
            org.spongepowered.asm.service.IMixinService service =
                    org.spongepowered.asm.service.MixinService.getService();
            Object provider = service.getBytecodeProvider();
            java.lang.reflect.Field field = findField(provider.getClass(), "transformerLoader");
            if (field == null) {
                trace("[PaperArc] SpawnReasonInjector: transformerLoader not found on "
                        + provider.getClass().getName() + " nor any superclass");
                return;
            }
            field.setAccessible(true);
            Object original = field.get(provider);
            if (original == null) {
                trace("[PaperArc] SpawnReasonInjector: transformerLoader is null; skipped");
                return;
            }
            ClassLoader providerCl = provider.getClass().getClassLoader();
            Class<?> iface = Class.forName(
                    "cpw.mods.modlauncher.serviceapi.ILaunchPluginService$ITransformerLoader",
                    false, providerCl);
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(providerCl,
                    new Class<?>[]{iface}, (p, method, args) -> {
                        if (method.getName().equals("buildTransformedClassNodeFor")
                                && args != null && args.length == 1
                                && TARGET.equals(args[0])) {
                            return bytes;
                        }
                        return method.invoke(original, args);
                    });
            field.set(provider, proxy);
            trace("[PaperArc] SpawnReasonInjector: hooked transformerLoader; "
                    + TARGET + " served via bytecode provider");
        } catch (Throwable t) {
            trace("[PaperArc] SpawnReasonInjector: hook transformerLoader failed: " + t);
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

    private static byte[] readResource() throws Exception {
        try (InputStream in = SpawnReasonInjector.class.getResourceAsStream(RESOURCE)) {
            return in == null ? null : in.readAllBytes();
        }
    }

    /** Traces to stderr AND a file so it is captured regardless of FML redirects. */
    private static void trace(String msg) {
        System.err.println(msg);
        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get("/tmp/paperarc-spawnreason.log"),
                    (java.time.LocalDateTime.now() + " " + msg + "\n").getBytes(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }
}
