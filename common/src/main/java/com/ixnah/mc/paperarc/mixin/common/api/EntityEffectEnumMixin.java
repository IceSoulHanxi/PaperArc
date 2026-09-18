package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcEnumConstants;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.bukkit.EntityEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Y-5 最后一批：{@code org.bukkit.EntityEffect} 的四个常量。
 *
 * <p><b>先分清"改名"还是"新增"</b>（docs/mixin-conventions.md 的 Y-5 教训）：
 * `javap -c` 对了两边的 {@code <clinit>} 之后 ——
 * <ul>
 *   <li>{@code TAMING_FAILED}(data 6) / {@code TAMING_SUCCEEDED}(data 7) 与运行时已有的
 *       {@code WOLF_SMOKE} / {@code WOLF_HEARTS} <b>data 与实体类型完全相同</b>，
 *       paper 只是加了一组更通用的名字（老名字它也留着）。所以走 {@code alias}：
 *       指向同一个实例，{@code values()} 不变、{@code switch}/{@code EnumMap} 全照旧；</li>
 *   <li>{@code ARMADILLO_PEEK}(data 64, {@code Armadillo}) 与 {@code BODY_BREAK}(data 65,
 *       {@code LivingEntity}) 是 1.20.5+ 真新增的，走 {@code EnumHelper.addEnum}，
 *       构造形参按**运行时**的构造器 {@code EntityEffect(int, Class)} 给。</li>
 * </ul>
 *
 * <p>消费方是现成的：{@code Entity#playEffect(EntityEffect)} →
 * {@code Level#broadcastEntityEvent(entity, effect.getData())}，
 * 所以 data 值必须对 —— 上面四个都是从 paper-api 的 {@code <clinit>} 抄的，不是编的。
 * {@code isApplicableTo(Entity)} 用的是那个 Class 形参，一并给对。
 */
@Mixin(EntityEffect.class)
public abstract class EntityEffectEnumMixin {

    @Unique
    @Widen(because = "paper-api: public static final EntityEffect TAMING_FAILED")
    private static EntityEffect TAMING_FAILED;

    @Unique
    @Widen(because = "paper-api: public static final EntityEffect TAMING_SUCCEEDED")
    private static EntityEffect TAMING_SUCCEEDED;

    @Unique
    @Widen(because = "paper-api: public static final EntityEffect ARMADILLO_PEEK")
    private static EntityEffect ARMADILLO_PEEK;

    @Unique
    @Widen(because = "paper-api: public static final EntityEffect BODY_BREAK")
    private static EntityEffect BODY_BREAK;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void paperarc$addPaperConstants(CallbackInfo ci) {
        TAMING_FAILED = PaperarcEnumConstants.alias(EntityEffect.class, "WOLF_SMOKE");
        TAMING_SUCCEEDED = PaperarcEnumConstants.alias(EntityEffect.class, "WOLF_HEARTS");
        ARMADILLO_PEEK = PaperarcEnumConstants.add(EntityEffect.class, "ARMADILLO_PEEK",
                List.of(int.class, Class.class),
                List.of(64, org.bukkit.entity.Armadillo.class));
        BODY_BREAK = PaperarcEnumConstants.add(EntityEffect.class, "BODY_BREAK",
                List.of(int.class, Class.class),
                List.of(65, org.bukkit.entity.LivingEntity.class));
    }
}
