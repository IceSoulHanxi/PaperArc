package com.ixnah.mc.paperarc.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

import java.nio.file.Files
import java.nio.file.Path

/**
 * 给 mixin 里 {@code @Unique public static} 的方法打上 ACC_SYNTHETIC。
 *
 * Mixin 0.8.5 的 MixinApplicatorStandard#checkMethodVisibility 直接拒绝把
 * "非 private 的静态方法" 合并进目标类：
 *   ACC_STATIC && !ACC_PRIVATE && !ACC_SYNTHETIC && 没有 @Overwrite -> InvalidMixinException
 * （javap -c 逐条核对；不加这一步，@Mixin(Bukkit.class) 上任何 public static 方法
 *  都会在 Apply Methods 阶段 FATAL，服务器起不来）。
 *
 * ACC_SYNTHETIC 是它显式放行的那一条。而 MixinPreProcessorStandard 里"给 @Unique /
 * synthetic 方法改名"那段有 {@code getVisibility(method) < PUBLIC} 的前置条件，
 * 所以 public + synthetic 的方法**不会**被改名（这是本做法成立的关键）。
 * 运行时 invokestatic 的解析不看 ACC_SYNTHETIC，插件照常调得到。
 *
 * 只处理 {@code com/ixnah/mc/paperarc/mixin/} 下的类，幂等（重复跑不会变化）。
 */
abstract class MarkSyntheticStaticApiTask extends DefaultTask {

    private static final String UNIQUE = 'Lorg/spongepowered/asm/mixin/Unique;'
    private static final String PREFIX = 'com/ixnah/mc/paperarc/mixin/'

    /** 就地改 compileJava 的输出，不声明成 input/output（会与 compileJava 的输出重叠）。 */
    @Internal
    abstract DirectoryProperty getClassesDir()

    @TaskAction
    void run() {
        Path root = classesDir.get().asFile.toPath()
        if (!Files.isDirectory(root)) {
            return
        }
        int patchedClasses = 0, patchedMethods = 0
        Files.walk(root).withCloseable { stream ->
            stream.filter { it.toString().endsWith('.class') }.forEach { Path path ->
                String internal = root.relativize(path).toString().replace(File.separator, '/')
                if (!internal.startsWith(PREFIX)) {
                    return
                }
                byte[] bytes = Files.readAllBytes(path)
                ClassNode node = new ClassNode()
                new ClassReader(bytes).accept(node, 0)
                int touched = 0
                for (MethodNode method : node.methods) {
                    boolean isPublicStatic = (method.access & Opcodes.ACC_STATIC) != 0 &&
                            (method.access & Opcodes.ACC_PUBLIC) != 0
                    boolean alreadySynthetic = (method.access & Opcodes.ACC_SYNTHETIC) != 0
                    boolean unique = method.visibleAnnotations?.any { it.desc == UNIQUE } ||
                            method.invisibleAnnotations?.any { it.desc == UNIQUE }
                    if (isPublicStatic && unique && !alreadySynthetic) {
                        method.access |= Opcodes.ACC_SYNTHETIC
                        touched++
                    }
                }
                if (touched > 0) {
                    ClassWriter writer = new ClassWriter(0)
                    node.accept(writer)
                    Files.write(path, writer.toByteArray())
                    patchedClasses++
                    patchedMethods += touched
                }
            }
        }
        logger.lifecycle("[markSyntheticStaticApi] ${patchedMethods} public static @Unique method(s) " +
                "in ${patchedClasses} mixin class(es) marked ACC_SYNTHETIC")
    }
}
