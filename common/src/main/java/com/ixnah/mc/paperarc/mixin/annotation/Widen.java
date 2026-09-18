package com.ixnah.mc.paperarc.mixin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个 {@code @Unique private} 成员：**合并进目标类之后**把访问级别放宽。
 *
 * <p>为什么需要：Mixin 的 {@code MixinApplicatorStandard.checkMethodVisibility} 直接拒绝
 * 把非 private 的 {@code static} 方法合并进目标类
 * （{@code InvalidMixinException: ... contains non-private static method ...}，
 * 而且只在运行时 APPLY 阶段炸）。paper 加在 {@code org.bukkit.Bukkit} / {@code ItemStack}
 * 上的静态门面方法因此补不进去（checklist §1.11 as）。写成 private 能合并，
 * 再在 {@code IMixinConfigPlugin.postApply}（所有 applicator pass 跑完、方法已进
 * 目标 ClassNode、字节还没写出）里把 {@code ACC_PRIVATE} 换成目标访问级别。</p>
 *
 * <p>为什么 AT/AW 不行：Forge AT 与 Fabric AW 都在 Mixin **之前**运行，而且只能改
 * 已经存在于目标类里的成员 —— 那时候我们的方法还没合并进去。</p>
 *
 * <p>{@code RetentionPolicy.CLASS}：进字节码（invisible annotation，ASM 读得到），
 * 不进反射；{@code WidenPostProcessor} 放宽之后会把它从方法上摘掉，
 * 让合并后的方法字节码与 paper-api 一致。</p>
 *
 * <p>约束（由 {@code tools/scripts/check-mixin-static-methods.py} 门禁保证）：
 * 只能修饰 {@code private} 成员；{@link #because()} 必须非空。</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Widen {

    /** 合并进目标类后放宽到的访问级别。 */
    Access value() default Access.PUBLIC;

    /** 为什么需要放宽（门禁要求非空）。 */
    String because() default "";
}
