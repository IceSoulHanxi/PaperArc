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
 * therefore wraps the modlauncher {@code ITransformerLoader} (FML 10: {@code BytecodeProvider}) owned by the mixin
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
        try {
            hookAndDefineSafe0();
        } catch (Throwable t) {
            // onLoad 抛出去 = 整个 mixin config 选择失败 = 服务器起不来。注入失败最多让
            // 某些 paper 类型缺失（插件报 NoClassDefFoundError），不该把服务器一起带走。
            hookDone = true;
            trace("[PaperArc] RuntimeClassInjector: phase1 aborted: " + t);
        }
    }

    private static void hookAndDefineSafe0() {
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
        try {
            defineDeferred0();
        } catch (Throwable t) {
            deferredDone = true;
            trace("[PaperArc] RuntimeClassInjector: phase2 aborted: " + t);
        }
    }

    private static void defineDeferred0() {
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

    /**
     * 按"被注入类型之间的继承关系"给 {@code classes} 排序，超类型排在前面。
     *
     * <p>JVM 在 define 一个类时必须先解析它的直接超类型（JVMS §5.3.5）。清单里
     * {@code BufferedCommandSender implements MessageCommandSender}、
     * {@code FeatureFlagImpl$Deprecated extends FeatureFlagImpl} 这种两个都要注入的情况，
     * 按 JSON 里的顺序 define 会先碰到子类 → {@code NoClassDefFoundError}（Forge 实测）。
     */
    private static Map<String, byte[]> inDependencyOrder(Map<String, byte[]> classes) {
        Map<String, byte[]> ordered = new LinkedHashMap<>();
        Set<String> visiting = new LinkedHashSet<>();
        for (String name : classes.keySet()) {
            visit(name, classes, ordered, visiting);
        }
        return ordered;
    }

    private static void visit(String name, Map<String, byte[]> classes,
            Map<String, byte[]> ordered, Set<String> visiting) {
        if (ordered.containsKey(name) || !visiting.add(name)) {
            return; // 已排好，或出现环（清单内不应有环，出现就按当前顺序放行）
        }
        for (String supertype : directSupertypes(name, classes.get(name))) {
            if (classes.containsKey(supertype)) {
                visit(supertype, classes, ordered, visiting);
            }
        }
        visiting.remove(name);
        ordered.put(name, classes.get(name));
    }

    /** 从字节里读直接超类与超接口（点分名）。 */
    private static java.util.List<String> directSupertypes(String name, byte[] bytes) {
        java.util.List<String> supers = new java.util.ArrayList<>();
        if (bytes == null) {
            return supers;
        }
        try {
            org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(bytes);
            if (reader.getSuperName() != null) {
                supers.add(reader.getSuperName().replace('/', '.'));
            }
            String[] interfaces = reader.getInterfaces();
            if (interfaces != null) {
                for (String itf : interfaces) {
                    supers.add(itf.replace('/', '.'));
                }
            }
        } catch (Throwable t) {
            trace("[PaperArc] RuntimeClassInjector: cannot read supertypes of " + name + ": " + t);
        }
        return supers;
    }

    /** defineClass {@code classes} into every reachable loader that does not have them yet. */
    private static void defineAll(String phase, Map<String, byte[]> input) {
        Map<String, byte[]> classes = inDependencyOrder(input);
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
                // 整段都要吞 Throwable，不能只吞 ClassNotFoundException：候选 loader 里混着
                // 引导期的 URLClassLoader（父链看不到 java.lang.Enum），存在性探测本身就会
                // 抛 NoClassDefFoundError —— 漏出去会让 onLoad 失败进而整个 mixin 变换失败，
                // Fabric 上实测直接起不来（Mixin transformation of FabricBootstrap failed）。
                try {
                    try {
                        Class.forName(name, false, loader);
                        continue; // already present in this loader
                    } catch (ClassNotFoundException expected) {
                        // not there yet -> define below
                    }
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
        for (String supertype : directSupertypes(name, bytes)) {
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
                // 后缀 .classbytes：loom 的 remapJar 会把 .class 条目按内部名重新落位，
                // 嵌在资源路径下的类字节保不住（见 common/build.gradle embedRuntimeClasses）
                String internal = name.replace('.', '/') + ".classbytes";
                String resource = "/META-INF/paperarc/runtime/" + internal;
                try (InputStream in = RuntimeClassInjector.class.getResourceAsStream(resource)) {
                    if (in == null) {
                        trace("[PaperArc] RuntimeClassInjector: bytes not found for " + name + " at " + resource);
                        continue;
                    }
                    map.put(name, stripNestAttributes(name, in.readAllBytes()));
                }
            }
        }
        return map;
    }

    /**
     * 剥掉嵌套类的 {@code InnerClasses} 自条目、{@code EnclosingMethod} 与 {@code NestHost}。
     *
     * <p>为什么必须剥：注入的是 paper-api 编译产物，`PlayerKickEvent$Cause` 的
     * InnerClasses 里写着"我的外围类是 PlayerKickEvent"，而运行时的
     * `PlayerKickEvent`（Arclight/spigot 版）根本没有这个嵌套类，它的 InnerClasses
     * 里也就没有对应条目。JVM 的 {@code Class#getDeclaringClass0} 会双向校验这对
     * 属性，一旦插件对注入类型调用 {@code getSimpleName()}/{@code getDeclaringClass()}
     * /{@code getEnclosingClass()} 就抛
     * {@code IncompatibleClassChangeError: … disagree on InnerClasses attribute}。
     * Debuggery v1.5.1 金丝雀实测：它遍历 API 方法签名时对每个参数类型调
     * {@code getSimpleName()}，直接导致插件 enable 失败（见任务书 A2-1 第五项）。
     *
     * <p>代价：这些类型的 {@code getSimpleName()} 变成二进制名（"PlayerKickEvent$Cause"
     * 而不是 "Cause"），{@code getDeclaringClass()} 返回 null。相比整插件崩掉可以接受；
     * 外围类在运行时已被 Arclight 的 mixin 管线加载/变换，去改它的 InnerClasses 不可行。
     */
    private static byte[] stripNestAttributes(String name, byte[] bytes) {
        String internal = name.replace('.', '/');
        if (internal.indexOf('$') < 0) {
            return bytes;
        }
        try {
            org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(bytes);
            org.objectweb.asm.ClassWriter writer = new org.objectweb.asm.ClassWriter(0);
            reader.accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9, writer) {
                @Override
                public void visitOuterClass(String owner, String methodName, String descriptor) {
                    // drop
                }

                @Override
                public void visitNestHost(String nestHost) {
                    // drop
                }

                @Override
                public void visitInnerClass(String innerName, String outerName, String simpleName, int access) {
                    if (!internal.equals(innerName)) {
                        super.visitInnerClass(innerName, outerName, simpleName, access);
                    }
                }
            }, 0);
            return writer.toByteArray();
        } catch (Throwable t) {
            trace("[PaperArc] RuntimeClassInjector: strip nest attributes failed for " + name + ": " + t);
            return bytes;
        }
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
     * Wraps the object the mixin service's bytecode provider reads class bytes from with a
     * dynamic proxy that serves embedded bytes for any configured class name, delegating
     * everything else to the original. Two shapes are supported:
     * <ul>
     *   <li>modlauncher (NeoForge 21.1 / Forge): field {@code transformerLoader} of type
     *       {@code ILaunchPluginService$ITransformerLoader}, method {@code buildTransformedClassNodeFor};</li>
     *   <li>FML 10 without modlauncher (NeoForge 21.11): {@code FMLClassBytecodeProvider} field
     *       {@code bytecodeProvider} of type {@code neoforgespi.transformation.BytecodeProvider},
     *       method {@code getByteCode}. Both receive the dotted class name.</li>
     * </ul>
     */
    private static void hookTransformerLoader(Map<String, byte[]> classes) {
        try {
            org.spongepowered.asm.service.IMixinService service =
                    org.spongepowered.asm.service.MixinService.getService();
            Object provider = service.getBytecodeProvider();
            java.lang.reflect.Field field = findField(provider.getClass(), "transformerLoader");
            String method = "buildTransformedClassNodeFor";
            if (field == null) {
                field = findField(provider.getClass(), "bytecodeProvider");
                method = "getByteCode";
            }
            if (field == null || !field.getType().isInterface()) {
                trace("[PaperArc] RuntimeClassInjector: neither transformerLoader nor bytecodeProvider on "
                        + provider.getClass().getName() + " nor any superclass");
                return;
            }
            field.setAccessible(true);
            Object original = field.get(provider);
            if (original == null) {
                trace("[PaperArc] RuntimeClassInjector: " + field.getName() + " is null; skipped");
                return;
            }
            Class<?> iface = field.getType();
            String served = method;
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(iface.getClassLoader(),
                    new Class<?>[]{iface}, (p, m, args) -> {
                        if (m.getName().equals(served) && args != null && args.length == 1) {
                            byte[] embedded = classes.get(args[0]);
                            if (embedded != null) {
                                return embedded;
                            }
                        }
                        try {
                            return m.invoke(original, args);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            // 必须解包：Mixin 用 ClassNotFoundException 表示"这个类不归我管"，
                            // 原样抛 InvocationTargetException 会被代理转成 UndeclaredThrowableException，
                            // 于是一次良性的未命中变成致命的 ClassMetadataNotFoundException
                            // （Forge/mixin 0.8.7 实测：Arclight 自己的 CraftBlockTypeMixin 直接 apply 失败）。
                            throw e.getCause() == null ? e : e.getCause();
                        }
                    });
            field.set(provider, proxy);
            trace("[PaperArc] RuntimeClassInjector: hooked " + field.getName() + "; "
                    + classes.keySet() + " served via bytecode provider");
        } catch (Throwable t) {
            trace("[PaperArc] RuntimeClassInjector: hook bytecode provider failed: " + t);
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
