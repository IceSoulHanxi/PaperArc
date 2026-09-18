package com.ixnah.mc.paperarc.bridge;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code @Widen} 的处理器：在 {@code IMixinConfigPlugin.postApply} 阶段把已经合并进
 * 目标类的 {@code private} 成员放宽到注解声明的访问级别，并把注解摘掉。
 *
 * <p>时序：{@code MixinApplicatorStandard.apply()} 先跑完所有 ApplicatorPass
 * （方法此时已经在 {@code targetClass.methods} 里），再逐个 mixin 调
 * {@code MixinTargetContext.postApply} → 配置插件的 {@code postApply}，
 * 最后才把 ClassNode 写成字节。所以这里改 access 是安全的、且三个加载器一致。</p>
 *
 * <p><b>断言</b>：mixin 自己的 ClassNode（{@link IMixinInfo#getClassNode(int)}）里每一个带
 * {@code @Widen} 的方法，都必须能在目标类里按**同名同描述符**找到。找不到只可能是
 * Mixin 因为冲突把 {@code @Unique private} 方法改了名
 * （{@code MixinPreProcessorStandard.attachUniqueMethod}：目标已有同签名方法且 mixin
 * 方法可见性低于 public 时会 rename），那样放宽就放到了一个插件永远调不到的名字上 ——
 * 直接抛异常让启动失败，不许静默漏过。</p>
 *
 * <p>一个目标类有 N 个 mixin 就会被调 N 次，但每次只看**当前这个 mixin 自己**声明的
 * {@code @Widen} 方法，各个 mixin 的集合互不相交，不会重复处理。</p>
 */
public final class WidenPostProcessor {

    /** {@code @Widen} 的字节码描述符；按字符串匹配，不需要把注解类装进来。 */
    private static final String WIDEN_DESC = "Lcom/ixnah/mc/paperarc/mixin/annotation/Widen;";

    /** 调试用：置 true 时只放宽、不摘注解，方便 javap -v 核对注解确实随合并进了目标。 */
    private static final boolean KEEP_ANNOTATION =
            Boolean.getBoolean("paperarc.widen.keepAnnotation");

    private static final int ACCESS_MASK = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE;

    private WidenPostProcessor() {
    }

    public static void postApply(String targetClassName, ClassNode targetClass, IMixinInfo mixinInfo) {
        ClassNode mixinNode = paperarc$mixinNode(mixinInfo);
        for (String[] key : paperarc$widenedMethods(mixinNode)) {
            MethodNode merged = paperarc$findMethod(targetClass, key[0], key[1]);
            if (merged == null) {
                throw new IllegalStateException("@Widen: " + mixinInfo.getClassName() + "#" + key[0] + key[1]
                        + " 没有按原名带着 @Widen 合并进 " + targetClassName
                        + "（Mixin 因签名冲突改了名，或目标本来就有同签名方法），拒绝静默放行");
            }
            paperarc$widenMethod(merged);
        }
        // B6-2：字段侧（B5 设计里"留口不实现"的那一半）。枚举常量补齐要求目标类上真的有一个
        // public static 字段，插件的 getstatic 才解析得到；判据与方法侧完全一致。
        for (String[] key : paperarc$widenedFields(mixinNode)) {
            FieldNode merged = paperarc$findField(targetClass, key[0], key[1]);
            if (merged == null) {
                throw new IllegalStateException("@Widen: " + mixinInfo.getClassName() + "#" + key[0] + " : " + key[1]
                        + " 没有按原名带着 @Widen 合并进 " + targetClassName
                        + "（Mixin 因冲突改了名，或目标本来就有同名字段），拒绝静默放行");
            }
            paperarc$widenField(merged);
        }
    }

    private static ClassNode paperarc$mixinNode(IMixinInfo mixinInfo) {
        try {
            return mixinInfo.getClassNode(0);
        } catch (Throwable t) {
            throw new IllegalStateException("@Widen: 读不到 mixin " + mixinInfo.getClassName() + " 的 ClassNode", t);
        }
    }

    /** 读 mixin 自己的 ClassNode，收集带 {@code @Widen} 的字段（名字 + 描述符）。 */
    private static List<String[]> paperarc$widenedFields(ClassNode mixinNode) {
        List<String[]> out = new ArrayList<>();
        if (mixinNode == null || mixinNode.fields == null) {
            return out;
        }
        for (FieldNode field : mixinNode.fields) {
            if (paperarc$widenAnnotation(field.invisibleAnnotations, field.visibleAnnotations) != null) {
                out.add(new String[]{field.name, field.desc});
            }
        }
        return out;
    }

    private static FieldNode paperarc$findField(ClassNode target, String name, String desc) {
        if (target.fields == null) {
            return null;
        }
        for (FieldNode field : target.fields) {
            if (name.equals(field.name) && desc.equals(field.desc)
                    && paperarc$widenAnnotation(field.invisibleAnnotations, field.visibleAnnotations) != null) {
                return field;
            }
        }
        return null;
    }

    private static void paperarc$widenField(FieldNode field) {
        AnnotationNode widen = paperarc$widenAnnotation(field.invisibleAnnotations, field.visibleAnnotations);
        field.access = (field.access & ~ACCESS_MASK) | paperarc$flag(paperarc$access(widen));
        if (!KEEP_ANNOTATION) {
            if (field.invisibleAnnotations != null) {
                field.invisibleAnnotations.remove(widen);
                if (field.invisibleAnnotations.isEmpty()) {
                    field.invisibleAnnotations = null;
                }
            }
            if (field.visibleAnnotations != null) {
                field.visibleAnnotations.remove(widen);
                if (field.visibleAnnotations.isEmpty()) {
                    field.visibleAnnotations = null;
                }
            }
        }
    }

    /** 读 mixin 自己的 ClassNode，收集带 {@code @Widen} 的方法（名字 + 描述符）。 */
    private static List<String[]> paperarc$widenedMethods(ClassNode mixinNode) {
        List<String[]> out = new ArrayList<>();
        if (mixinNode == null || mixinNode.methods == null) {
            return out;
        }
        for (MethodNode method : mixinNode.methods) {
            if (paperarc$widenAnnotation(method.invisibleAnnotations, method.visibleAnnotations) != null) {
                out.add(new String[]{method.name, method.desc});
            }
        }
        return out;
    }

    private static AnnotationNode paperarc$widenAnnotation(List<AnnotationNode> invisible, List<AnnotationNode> visible) {
        if (invisible != null) {
            for (AnnotationNode node : invisible) {
                if (WIDEN_DESC.equals(node.desc)) {
                    return node;
                }
            }
        }
        // RetentionPolicy.CLASS 本该落在 invisible 一侧，这里兜一下防止编译器行为差异
        if (visible != null) {
            for (AnnotationNode node : visible) {
                if (WIDEN_DESC.equals(node.desc)) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * 按名字 + 描述符 + <b>带着 {@code @Widen}</b> 找。三个条件缺一不可：
     * 只比名字和描述符的话，"Mixin 把我们的方法改了名、而目标里原本就有一个同签名方法"
     * 这种情况会命中那个原有方法 —— 放宽变成空操作、真正合并进去的方法插件永远调不到，
     * 断言却一无所知（B5-1 反向实验实测）。
     */
    private static MethodNode paperarc$findMethod(ClassNode target, String name, String desc) {
        for (MethodNode method : target.methods) {
            if (name.equals(method.name) && desc.equals(method.desc)
                    && paperarc$widenAnnotation(method.invisibleAnnotations, method.visibleAnnotations) != null) {
                return method;
            }
        }
        return null;
    }

    private static void paperarc$widenMethod(MethodNode method) {
        AnnotationNode widen = paperarc$widenAnnotation(method.invisibleAnnotations, method.visibleAnnotations);
        method.access = (method.access & ~ACCESS_MASK) | paperarc$flag(paperarc$access(widen));
        if (!KEEP_ANNOTATION) {
            if (method.invisibleAnnotations != null) {
                method.invisibleAnnotations.remove(widen);
                if (method.invisibleAnnotations.isEmpty()) {
                    method.invisibleAnnotations = null;
                }
            }
            if (method.visibleAnnotations != null) {
                method.visibleAnnotations.remove(widen);
                if (method.visibleAnnotations.isEmpty()) {
                    method.visibleAnnotations = null;
                }
            }
        }
    }

    /** 读注解的 {@code value}（枚举以 {@code [desc, name]} 的形式存在 values 里）。 */
    private static String paperarc$access(AnnotationNode widen) {
        if (widen.values != null) {
            for (int i = 0; i + 1 < widen.values.size(); i += 2) {
                if ("value".equals(widen.values.get(i)) && widen.values.get(i + 1) instanceof String[] enumValue
                        && enumValue.length == 2) {
                    return enumValue[1];
                }
            }
        }
        return "PUBLIC";                 // 注解默认值不会写进 values
    }

    private static int paperarc$flag(String access) {
        switch (access) {
            case "PROTECTED":
                return Opcodes.ACC_PROTECTED;
            case "PACKAGE":
                return 0;
            default:
                return Opcodes.ACC_PUBLIC;
        }
    }
}
