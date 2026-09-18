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
 * paper 给 {@code org.bukkit.Effect} 新增的 19 个常量（A8/Y-5；A7 因为两个原因搁置）。
 *
 * <p>与 {@code DisplaySlot} 不同，这 19 个**是真的新常量**（虽然其中 6 个是老常量的改名，
 * 但 paper 把新旧两套都留着、id 相同），所以走 {@code EnumHelper} 的"造常量"配方，
 * 不是别名。两个 A7 挡住的点都补齐了：
 * <ol>
 *   <li>运行时 {@code Effect} 的构造器带形参（{@code (int id, Effect.Type)} 与
 *       {@code (int, Effect.Type, Class)}，`javap -s` 核对描述符里隐含的
 *       {@code (String name, int ordinal)} 由 EnumHelper 自己补），
 *       于是给 {@link PaperarcEnumConstants} 加了带实参的重载；</li>
 *   <li>{@code <clinit>} 里那张 {@code BY_ID} 表是 {@code Effect.getById} 与
 *       {@code CraftWorld#playEffect} 的入口，光扩 {@code ENUM$VALUES} 等于"取得到但播不出来"。
 *       这里 {@code @Shadow} 它并在 TAIL 一起补上。</li>
 * </ol>
 *
 * <p>id 与 Type 全部逐条抄自 paper-api 的 {@code <clinit>} 字节码（`javap -c`），
 * 不是猜的。**消费方**：{@code World#playEffect} 走 {@code getId()} 发
 * vanilla 的 LevelEvent，新常量的 id 都是真的世界事件号，播得出来。
 *
 * <p>与 paper 的已知差异：改名那 6 对（如 {@code PHANTOM_BITE}/{@code PHANTOM_BITES}）
 * id 相同，{@code BY_ID} 是 {@code Map<Integer, Effect>}，后写入的赢 ——
 * 我们在 TAIL 追加，所以 {@code getById} 返回新名，与 paper 的声明顺序一致。
 */
@Mixin(Effect.class)
public abstract class EffectEnumMixin {

    @Shadow
    @Final
    private static Map<Integer, Effect> BY_ID;

    @Unique
    @Widen(because = "paper-api: public static final Effect BOOK_PAGE_TURNED")
    private static Effect BOOK_PAGE_TURNED;

    @Unique
    @Widen(because = "paper-api: public static final Effect COMPOSTER_COMPOSTS")
    private static Effect COMPOSTER_COMPOSTS;

    @Unique
    @Widen(because = "paper-api: public static final Effect ENDER_DRAGON_DEATH")
    private static Effect ENDER_DRAGON_DEATH;

    @Unique
    @Widen(because = "paper-api: public static final Effect ENDER_DRAGON_DESTROYS_BLOCK")
    private static Effect ENDER_DRAGON_DESTROYS_BLOCK;

    @Unique
    @Widen(because = "paper-api: public static final Effect ENDER_EYE_PLACED")
    private static Effect ENDER_EYE_PLACED;

    @Unique
    @Widen(because = "paper-api: public static final Effect END_PORTAL_CREATED_IN_OVERWORLD")
    private static Effect END_PORTAL_CREATED_IN_OVERWORLD;

    @Unique
    @Widen(because = "paper-api: public static final Effect GRINDSTONE_USED")
    private static Effect GRINDSTONE_USED;

    @Unique
    @Widen(because = "paper-api: public static final Effect HUSK_CONVERTS_TO_ZOMBIE")
    private static Effect HUSK_CONVERTS_TO_ZOMBIE;

    @Unique
    @Widen(because = "paper-api: public static final Effect LAVA_CONVERTS_BLOCK")
    private static Effect LAVA_CONVERTS_BLOCK;

    @Unique
    @Widen(because = "paper-api: public static final Effect PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE")
    private static Effect PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE;

    @Unique
    @Widen(because = "paper-api: public static final Effect PARTICLES_EGG_CRACK")
    private static Effect PARTICLES_EGG_CRACK;

    @Unique
    @Widen(because = "paper-api: public static final Effect PARTICLES_SCULK_CHARGE")
    private static Effect PARTICLES_SCULK_CHARGE;

    @Unique
    @Widen(because = "paper-api: public static final Effect PARTICLES_SCULK_SHRIEK")
    private static Effect PARTICLES_SCULK_SHRIEK;

    @Unique
    @Widen(because = "paper-api: public static final Effect PHANTOM_BITES")
    private static Effect PHANTOM_BITES;

    @Unique
    @Widen(because = "paper-api: public static final Effect REDSTONE_TORCH_BURNS_OUT")
    private static Effect REDSTONE_TORCH_BURNS_OUT;

    @Unique
    @Widen(because = "paper-api: public static final Effect SOUND_STOP_JUKEBOX_SONG")
    private static Effect SOUND_STOP_JUKEBOX_SONG;

    @Unique
    @Widen(because = "paper-api: public static final Effect WET_SPONGE_VAPORIZES_IN_NETHER")
    private static Effect WET_SPONGE_VAPORIZES_IN_NETHER;

    @Unique
    @Widen(because = "paper-api: public static final Effect WITHER_SPAWNED")
    private static Effect WITHER_SPAWNED;

    @Unique
    @Widen(because = "paper-api: public static final Effect ZOMBIE_CONVERTS_TO_DROWNED")
    private static Effect ZOMBIE_CONVERTS_TO_DROWNED;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void paperarc$addPaperConstants(CallbackInfo ci) {
        BOOK_PAGE_TURNED = PaperarcEnumConstants.add(Effect.class, "BOOK_PAGE_TURNED",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1043, Effect.Type.SOUND));
        COMPOSTER_COMPOSTS = PaperarcEnumConstants.add(Effect.class, "COMPOSTER_COMPOSTS",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1500, Effect.Type.VISUAL));
        ENDER_DRAGON_DEATH = PaperarcEnumConstants.add(Effect.class, "ENDER_DRAGON_DEATH",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1028, Effect.Type.SOUND));
        ENDER_DRAGON_DESTROYS_BLOCK = PaperarcEnumConstants.add(Effect.class, "ENDER_DRAGON_DESTROYS_BLOCK",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(2008, Effect.Type.VISUAL));
        ENDER_EYE_PLACED = PaperarcEnumConstants.add(Effect.class, "ENDER_EYE_PLACED",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1503, Effect.Type.VISUAL));
        END_PORTAL_CREATED_IN_OVERWORLD = PaperarcEnumConstants.add(Effect.class, "END_PORTAL_CREATED_IN_OVERWORLD",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1038, Effect.Type.SOUND));
        GRINDSTONE_USED = PaperarcEnumConstants.add(Effect.class, "GRINDSTONE_USED",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1042, Effect.Type.SOUND));
        HUSK_CONVERTS_TO_ZOMBIE = PaperarcEnumConstants.add(Effect.class, "HUSK_CONVERTS_TO_ZOMBIE",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1041, Effect.Type.SOUND));
        LAVA_CONVERTS_BLOCK = PaperarcEnumConstants.add(Effect.class, "LAVA_CONVERTS_BLOCK",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1501, Effect.Type.VISUAL));
        PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE = PaperarcEnumConstants.add(Effect.class, "PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3008, Effect.Type.VISUAL, org.bukkit.block.data.BlockData.class));
        PARTICLES_EGG_CRACK = PaperarcEnumConstants.add(Effect.class, "PARTICLES_EGG_CRACK",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(3009, Effect.Type.VISUAL));
        PARTICLES_SCULK_CHARGE = PaperarcEnumConstants.add(Effect.class, "PARTICLES_SCULK_CHARGE",
                java.util.List.of(int.class, Effect.Type.class, Class.class),
                java.util.List.of(3006, Effect.Type.VISUAL, java.lang.Integer.class));
        PARTICLES_SCULK_SHRIEK = PaperarcEnumConstants.add(Effect.class, "PARTICLES_SCULK_SHRIEK",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(3007, Effect.Type.SOUND));
        PHANTOM_BITES = PaperarcEnumConstants.add(Effect.class, "PHANTOM_BITES",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1039, Effect.Type.SOUND));
        REDSTONE_TORCH_BURNS_OUT = PaperarcEnumConstants.add(Effect.class, "REDSTONE_TORCH_BURNS_OUT",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1502, Effect.Type.VISUAL));
        SOUND_STOP_JUKEBOX_SONG = PaperarcEnumConstants.add(Effect.class, "SOUND_STOP_JUKEBOX_SONG",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1011, Effect.Type.SOUND));
        WET_SPONGE_VAPORIZES_IN_NETHER = PaperarcEnumConstants.add(Effect.class, "WET_SPONGE_VAPORIZES_IN_NETHER",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(2009, Effect.Type.VISUAL));
        WITHER_SPAWNED = PaperarcEnumConstants.add(Effect.class, "WITHER_SPAWNED",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1023, Effect.Type.SOUND));
        ZOMBIE_CONVERTS_TO_DROWNED = PaperarcEnumConstants.add(Effect.class, "ZOMBIE_CONVERTS_TO_DROWNED",
                java.util.List.of(int.class, Effect.Type.class),
                java.util.List.of(1040, Effect.Type.SOUND));

        // BY_ID 是 Effect.getById / CraftWorld#playEffect 的查表入口，必须一起补
        BY_ID.put(1043, BOOK_PAGE_TURNED);
        BY_ID.put(1500, COMPOSTER_COMPOSTS);
        BY_ID.put(1028, ENDER_DRAGON_DEATH);
        BY_ID.put(2008, ENDER_DRAGON_DESTROYS_BLOCK);
        BY_ID.put(1503, ENDER_EYE_PLACED);
        BY_ID.put(1038, END_PORTAL_CREATED_IN_OVERWORLD);
        BY_ID.put(1042, GRINDSTONE_USED);
        BY_ID.put(1041, HUSK_CONVERTS_TO_ZOMBIE);
        BY_ID.put(1501, LAVA_CONVERTS_BLOCK);
        BY_ID.put(3008, PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE);
        BY_ID.put(3009, PARTICLES_EGG_CRACK);
        BY_ID.put(3006, PARTICLES_SCULK_CHARGE);
        BY_ID.put(3007, PARTICLES_SCULK_SHRIEK);
        BY_ID.put(1039, PHANTOM_BITES);
        BY_ID.put(1502, REDSTONE_TORCH_BURNS_OUT);
        BY_ID.put(1011, SOUND_STOP_JUKEBOX_SONG);
        BY_ID.put(2009, WET_SPONGE_VAPORIZES_IN_NETHER);
        BY_ID.put(1023, WITHER_SPAWNED);
        BY_ID.put(1040, ZOMBIE_CONVERTS_TO_DROWNED);
    }
}
