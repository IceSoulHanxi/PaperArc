package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcEnumConstants;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.bukkit.Effect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Y-5：给 {@code org.bukkit.Effect} 补 paper 新增的 27 个常量。
 *
 * <p><b>这一组是"有消费方"的</b>：{@code World#playEffect(Location, Effect, T)} 走的是
 * {@code CraftWorld → Level#levelEvent(effect.getId(), …)}，所以常量必须带着**正确的
 * vanilla LevelEvent id** 才真能用。id 与 {@code Effect.Type}/data 类逐条抄自 paper-api 的
 * {@code Effect.<clinit>}（javap 提取，见任务书 Y-5 结果行），不是自己编的。
 *
 * <p>运行时 {@code Effect} 的构造器是 {@code (int, Effect.Type)} / {@code (int, Effect.Type, Class)}，
 * 所以用 {@code PaperarcEnumConstants#add} 的带参重载。
 *
 * <p>额外一步：{@code Effect.getById(int)} 靠 {@code <clinit>} 里建好的 {@code BY_ID} 映射，
 * 我们在 TAIL 追加的常量不在里面 —— 一并 {@code put} 进去，否则 {@code getById} 会返回 null。
 */
@Mixin(Effect.class)
public abstract class EffectEnumMixin {

    @Shadow(remap = false)
    @Final
    private static Map<Integer, Effect> BY_ID;

    @Unique
    @Widen(because = "paper-api: public static final Effect SOUND_STOP_JUKEBOX_SONG")
    private static Effect SOUND_STOP_JUKEBOX_SONG;

    @Unique
    @Widen(because = "paper-api: public static final Effect WITHER_SPAWNED")
    private static Effect WITHER_SPAWNED;

    @Unique
    @Widen(because = "paper-api: public static final Effect ENDER_DRAGON_DEATH")
    private static Effect ENDER_DRAGON_DEATH;

    @Unique
    @Widen(because = "paper-api: public static final Effect END_PORTAL_CREATED_IN_OVERWORLD")
    private static Effect END_PORTAL_CREATED_IN_OVERWORLD;

    @Unique
    @Widen(because = "paper-api: public static final Effect CRAFTER_CRAFT")
    private static Effect CRAFTER_CRAFT;

    @Unique
    @Widen(because = "paper-api: public static final Effect CRAFTER_FAIL")
    private static Effect CRAFTER_FAIL;

    @Unique
    @Widen(because = "paper-api: public static final Effect SOUND_WITH_CHARGE_SHOT")
    private static Effect SOUND_WITH_CHARGE_SHOT;

    @Unique
    @Widen(because = "paper-api: public static final Effect SHOOT_WHITE_SMOKE")
    private static Effect SHOOT_WHITE_SMOKE;

    @Unique
    @Widen(because = "paper-api: public static final Effect BEE_GROWTH")
    private static Effect BEE_GROWTH;

    @Unique
    @Widen(because = "paper-api: public static final Effect TURTLE_EGG_PLACEMENT")
    private static Effect TURTLE_EGG_PLACEMENT;

    @Unique
    @Widen(because = "paper-api: public static final Effect SMASH_ATTACK")
    private static Effect SMASH_ATTACK;

    @Unique
    @Widen(because = "paper-api: public static final Effect PARTICLES_SCULK_CHARGE")
    private static Effect PARTICLES_SCULK_CHARGE;

    @Unique
    @Widen(because = "paper-api: public static final Effect PARTICLES_SCULK_SHRIEK")
    private static Effect PARTICLES_SCULK_SHRIEK;

    @Unique
    @Widen(because = "paper-api: public static final Effect PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE")
    private static Effect PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE;

    @Unique
    @Widen(because = "paper-api: public static final Effect PARTICLES_EGG_CRACK")
    private static Effect PARTICLES_EGG_CRACK;

    @Unique
    @Widen(because = "paper-api: public static final Effect GUST_DUST")
    private static Effect GUST_DUST;

    @Unique
    @Widen(because = "paper-api: public static final Effect TRIAL_SPAWNER_SPAWN")
    private static Effect TRIAL_SPAWNER_SPAWN;

    @Unique
    @Widen(because = "paper-api: public static final Effect TRIAL_SPAWNER_SPAWN_MOB_AT")
    private static Effect TRIAL_SPAWNER_SPAWN_MOB_AT;

    @Unique
    @Widen(because = "paper-api: public static final Effect TRIAL_SPAWNER_DETECT_PLAYER")
    private static Effect TRIAL_SPAWNER_DETECT_PLAYER;

    @Unique
    @Widen(because = "paper-api: public static final Effect TRIAL_SPAWNER_EJECT_ITEM")
    private static Effect TRIAL_SPAWNER_EJECT_ITEM;

    @Unique
    @Widen(because = "paper-api: public static final Effect VAULT_ACTIVATE")
    private static Effect VAULT_ACTIVATE;

    @Unique
    @Widen(because = "paper-api: public static final Effect VAULT_DEACTIVATE")
    private static Effect VAULT_DEACTIVATE;

    @Unique
    @Widen(because = "paper-api: public static final Effect VAULT_EJECT_ITEM")
    private static Effect VAULT_EJECT_ITEM;

    @Unique
    @Widen(because = "paper-api: public static final Effect SPAWN_COBWEB")
    private static Effect SPAWN_COBWEB;

    @Unique
    @Widen(because = "paper-api: public static final Effect TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS")
    private static Effect TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS;

    @Unique
    @Widen(because = "paper-api: public static final Effect TRIAL_SPAWNER_BECOME_OMINOUS")
    private static Effect TRIAL_SPAWNER_BECOME_OMINOUS;

    @Unique
    @Widen(because = "paper-api: public static final Effect TRIAL_SPAWNER_SPAWN_ITEM")
    private static Effect TRIAL_SPAWNER_SPAWN_ITEM;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void paperarc$addPaperConstants(CallbackInfo ci) {
        SOUND_STOP_JUKEBOX_SONG = PaperarcEnumConstants.add(Effect.class, "SOUND_STOP_JUKEBOX_SONG",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1011, Effect.Type.SOUND));
        BY_ID.put(1011, SOUND_STOP_JUKEBOX_SONG);
        WITHER_SPAWNED = PaperarcEnumConstants.add(Effect.class, "WITHER_SPAWNED",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1023, Effect.Type.SOUND));
        BY_ID.put(1023, WITHER_SPAWNED);
        ENDER_DRAGON_DEATH = PaperarcEnumConstants.add(Effect.class, "ENDER_DRAGON_DEATH",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1028, Effect.Type.SOUND));
        BY_ID.put(1028, ENDER_DRAGON_DEATH);
        END_PORTAL_CREATED_IN_OVERWORLD = PaperarcEnumConstants.add(Effect.class, "END_PORTAL_CREATED_IN_OVERWORLD",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1038, Effect.Type.SOUND));
        BY_ID.put(1038, END_PORTAL_CREATED_IN_OVERWORLD);
        CRAFTER_CRAFT = PaperarcEnumConstants.add(Effect.class, "CRAFTER_CRAFT",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1049, Effect.Type.SOUND));
        BY_ID.put(1049, CRAFTER_CRAFT);
        CRAFTER_FAIL = PaperarcEnumConstants.add(Effect.class, "CRAFTER_FAIL",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1050, Effect.Type.SOUND));
        BY_ID.put(1050, CRAFTER_FAIL);
        SOUND_WITH_CHARGE_SHOT = PaperarcEnumConstants.add(Effect.class, "SOUND_WITH_CHARGE_SHOT",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1051, Effect.Type.SOUND));
        BY_ID.put(1051, SOUND_WITH_CHARGE_SHOT);
        SHOOT_WHITE_SMOKE = PaperarcEnumConstants.add(Effect.class, "SHOOT_WHITE_SMOKE",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(2010, Effect.Type.VISUAL, org.bukkit.block.BlockFace.class));
        BY_ID.put(2010, SHOOT_WHITE_SMOKE);
        BEE_GROWTH = PaperarcEnumConstants.add(Effect.class, "BEE_GROWTH",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(2011, Effect.Type.VISUAL, java.lang.Integer.class));
        BY_ID.put(2011, BEE_GROWTH);
        TURTLE_EGG_PLACEMENT = PaperarcEnumConstants.add(Effect.class, "TURTLE_EGG_PLACEMENT",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(2012, Effect.Type.VISUAL, java.lang.Integer.class));
        BY_ID.put(2012, TURTLE_EGG_PLACEMENT);
        SMASH_ATTACK = PaperarcEnumConstants.add(Effect.class, "SMASH_ATTACK",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(2013, Effect.Type.VISUAL, java.lang.Integer.class));
        BY_ID.put(2013, SMASH_ATTACK);
        PARTICLES_SCULK_CHARGE = PaperarcEnumConstants.add(Effect.class, "PARTICLES_SCULK_CHARGE",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3006, Effect.Type.VISUAL, java.lang.Integer.class));
        BY_ID.put(3006, PARTICLES_SCULK_CHARGE);
        PARTICLES_SCULK_SHRIEK = PaperarcEnumConstants.add(Effect.class, "PARTICLES_SCULK_SHRIEK",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(3007, Effect.Type.SOUND));
        BY_ID.put(3007, PARTICLES_SCULK_SHRIEK);
        PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE = PaperarcEnumConstants.add(Effect.class, "PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3008, Effect.Type.VISUAL, org.bukkit.block.data.BlockData.class));
        BY_ID.put(3008, PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE);
        PARTICLES_EGG_CRACK = PaperarcEnumConstants.add(Effect.class, "PARTICLES_EGG_CRACK",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(3009, Effect.Type.VISUAL));
        BY_ID.put(3009, PARTICLES_EGG_CRACK);
        GUST_DUST = PaperarcEnumConstants.add(Effect.class, "GUST_DUST",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(3010, Effect.Type.VISUAL));
        BY_ID.put(3010, GUST_DUST);
        TRIAL_SPAWNER_SPAWN = PaperarcEnumConstants.add(Effect.class, "TRIAL_SPAWNER_SPAWN",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3011, Effect.Type.VISUAL, java.lang.Boolean.class));
        BY_ID.put(3011, TRIAL_SPAWNER_SPAWN);
        TRIAL_SPAWNER_SPAWN_MOB_AT = PaperarcEnumConstants.add(Effect.class, "TRIAL_SPAWNER_SPAWN_MOB_AT",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3012, Effect.Type.VISUAL, java.lang.Boolean.class));
        BY_ID.put(3012, TRIAL_SPAWNER_SPAWN_MOB_AT);
        TRIAL_SPAWNER_DETECT_PLAYER = PaperarcEnumConstants.add(Effect.class, "TRIAL_SPAWNER_DETECT_PLAYER",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3013, Effect.Type.VISUAL, java.lang.Integer.class));
        BY_ID.put(3013, TRIAL_SPAWNER_DETECT_PLAYER);
        TRIAL_SPAWNER_EJECT_ITEM = PaperarcEnumConstants.add(Effect.class, "TRIAL_SPAWNER_EJECT_ITEM",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(3014, Effect.Type.VISUAL));
        BY_ID.put(3014, TRIAL_SPAWNER_EJECT_ITEM);
        VAULT_ACTIVATE = PaperarcEnumConstants.add(Effect.class, "VAULT_ACTIVATE",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3015, Effect.Type.VISUAL, java.lang.Boolean.class));
        BY_ID.put(3015, VAULT_ACTIVATE);
        VAULT_DEACTIVATE = PaperarcEnumConstants.add(Effect.class, "VAULT_DEACTIVATE",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3016, Effect.Type.VISUAL, java.lang.Boolean.class));
        BY_ID.put(3016, VAULT_DEACTIVATE);
        VAULT_EJECT_ITEM = PaperarcEnumConstants.add(Effect.class, "VAULT_EJECT_ITEM",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(3017, Effect.Type.VISUAL));
        BY_ID.put(3017, VAULT_EJECT_ITEM);
        SPAWN_COBWEB = PaperarcEnumConstants.add(Effect.class, "SPAWN_COBWEB",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(3018, Effect.Type.VISUAL));
        BY_ID.put(3018, SPAWN_COBWEB);
        TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS = PaperarcEnumConstants.add(Effect.class, "TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3019, Effect.Type.VISUAL, java.lang.Integer.class));
        BY_ID.put(3019, TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS);
        TRIAL_SPAWNER_BECOME_OMINOUS = PaperarcEnumConstants.add(Effect.class, "TRIAL_SPAWNER_BECOME_OMINOUS",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3020, Effect.Type.VISUAL, java.lang.Boolean.class));
        BY_ID.put(3020, TRIAL_SPAWNER_BECOME_OMINOUS);
        TRIAL_SPAWNER_SPAWN_ITEM = PaperarcEnumConstants.add(Effect.class, "TRIAL_SPAWNER_SPAWN_ITEM",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3021, Effect.Type.VISUAL, java.lang.Boolean.class));
        BY_ID.put(3021, TRIAL_SPAWNER_SPAWN_ITEM);
    }
}
