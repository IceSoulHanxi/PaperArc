package com.ixnah.mc.paperarc.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

/**
 * Renames the deobf CraftBukkit package to the server's versioned one.
 *
 * Sources compile against the deobf spigot artifact where Craft* classes live
 * under org/bukkit/craftbukkit/v/. Arclight serves them as
 * org/bukkit/craftbukkit/<rev>/ and does NOT remap third-party mixins, so every
 * v/ reference that reaches a resolved class must be rewritten post-build.
 *
 * This mirrors Arclight's own RemapSpigotTask, which feeds SpecialSource the
 * single package mapping "PK: org/bukkit/craftbukkit/v org/bukkit/craftbukkit/<rev>".
 * We apply the identical rename with an ASM Remapper (annotations, descriptors,
 * signatures included) plus an extra pass loom's pipeline needs and SpecialSource
 * cannot see: the mixin refmap JSON, whose values keep unversioned CB owners.
 */
abstract class VersionCraftBukkitTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    abstract RegularFileProperty getJar()

    @Input
    abstract Property<String> getRevision()

    /** Optional comma-separated mixin config names written into MANIFEST.MF (Forge needs this). */
    @Input
    abstract Property<String> getMixinConfigs()

    private static final String SLASH = 'org/bukkit/craftbukkit/v/'
    private static final String DOT = 'org.bukkit.craftbukkit.v.'

    @TaskAction
    void run() {
        def rev = revision.get()
        def src = jar.get().asFile.toPath()
        def dst = Files.createTempFile('paperarc-cb', '.jar')
        int classes = 0, refmaps = 0
        boolean sawManifest = false
        try (def zin = new JarInputStream(src.newInputStream())) {
            try (def zout = new JarOutputStream(dst.newOutputStream())) {
                JarEntry e
                def buffer = new byte[1 << 16]
                while ((e = zin.nextJarEntry) != null) {
                    def data = zin.readAllBytes()
                    if (e.name.endsWith('.class')) {
                        def rw = rewriteClass(data, rev)
                        if (rw != null) { data = rw; classes++ }
                    } else if (e.name == 'META-INF/MANIFEST.MF') {
                        def text = new String(data, java.nio.charset.StandardCharsets.UTF_8)
                        def cfg = mixinConfigs.getOrNull()
                        if (cfg && !text.contains('MixinConfigs:')) {
                            if (!text.endsWith('\n')) text += '\n'
                            text += "MixinConfigs: ${cfg}\n"
                        }
                        data = text.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                    } else if (e.name.endsWith('-refmap.json')) {
                        def text = new String(data, java.nio.charset.StandardCharsets.UTF_8)
                        if (text.contains(SLASH) || text.contains(DOT)) {
                            text = text.replace(SLASH, "org/bukkit/craftbukkit/${rev}/")
                                       .replace(DOT, "org.bukkit.craftbukkit.${rev}.")
                            data = text.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                            refmaps++
                        }
                    }
                    def out = new JarEntry(e.name)
                    zout.putNextEntry(out)
                    zout.write(data)
                    zout.closeEntry()
                }
                if (!sawManifest && mixinConfigs.getOrNull()) {
                    sawManifest = true
                    zout.putNextEntry(new JarEntry('META-INF/MANIFEST.MF'))
                    zout.write(("Manifest-Version: 1.0\nMixinConfigs: ${mixinConfigs.get()}\n")
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    zout.closeEntry()
                    println '[versionCraftBukkit] manifest MixinConfigs injected'
                }
            }
        }
        java.nio.file.Files.move(dst, src, StandardCopyOption.REPLACE_EXISTING)
        println "[versionCraftBukkit] rewrote ${classes} class files, ${refmaps} refmaps -> ${rev}"
    }

    /** Returns rewritten bytes or null when untouched. */
    private static byte[] rewriteClass(byte[] bytes, String rev) {
        if (!new String(bytes, 0, Math.min(bytes.length, 4000), java.nio.charset.StandardCharsets.ISO_8859_1)
                .contains('craftbukkit/v') && !containsUtf8Dot(bytes)) {
            // cheap pre-filter: both forms embed 'craftbukkit/v' or 'craftbukkit.v.'
            if (!new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1).contains('craftbukkit')) return null
        }
        def mapper = new Remapper() {
            @Override
            String map(String internalName) {
                if (internalName == 'org/bukkit/craftbukkit/v') return "org/bukkit/craftbukkit/${rev}".toString()
                if (internalName.startsWith(SLASH)) return ("org/bukkit/craftbukkit/${rev}/" + internalName.substring(SLASH.length())).toString()
                return internalName
            }

            @Override
            Object mapValue(Object value) {
                if (value instanceof String) {
                    if (value.startsWith(DOT)) return ("org.bukkit.craftbukkit.${rev}." + value.substring(DOT.length())).toString()
                    if (value.contains(DOT)) return value.replace(DOT, "org.bukkit.craftbukkit.${rev}.").toString()
                    if (value.contains(SLASH)) return value.replace(SLASH, "org/bukkit/craftbukkit/${rev}/").toString()
                }
                return super.mapValue(value)
            }
        }
        def reader = new ClassReader(bytes)
        def writer = new ClassWriter(0)
        reader.accept(new ClassRemapper(writer, mapper), 0)
        return writer.toByteArray()
    }

    private static boolean containsUtf8Dot(byte[] bytes) {
        return indexOf(bytes, 'craftbukkit.v.'.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)) >= 0
    }

    private static int indexOf(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer
            }
            return i
        }
        return -1
    }
}
