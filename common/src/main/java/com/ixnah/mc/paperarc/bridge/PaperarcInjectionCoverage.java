package com.ixnah.mc.paperarc.bridge;

import java.util.ArrayList;
import java.util.List;

/**
 * 把"只在有玩家时才会被加载"的注入目标显式拉起来一次（checklist bi）。
 *
 * <p>{@code -Dmixin.debug.countInjections=true} 的注入校验只在类**被变换时**才跑：
 * {@code ServerLoginPacketListenerImpl}、{@code PlayerAdvancements}、
 * {@code CraftHumanEntity} 这些类在无人服上根本不加载，锚点写错也不会有任何报错，
 * 门禁一路绿灯，等真有人进服才炸。A7 是在探针里 {@code Class.forName} 顶上的；
 * A8 按 main/B7 的做法收敛到 mod 侧这一处清单，两个分支好对照
 * （main 那边是 {@code PaperarcInjectionCoverage#loadAll()} 同名同职责）。
 *
 * <p>调用时机是 Forge 的 {@code ServerStartedEvent}：再早就可能在 mixin config
 * 还没 prepare 完的时候把 {@code org.bukkit.*} 拉进来，触发
 * {@code MixinTargetAlreadyLoadedException}（A6 踩过）。
 */
public final class PaperarcInjectionCoverage {

    private PaperarcInjectionCoverage() {
    }

    /** NMS 侧：这些类的 mixin 锚点只有玩家登录/交互时才会被验证。 */
    private static final String[] NMS_CLASSES = {
            "net.minecraft.server.network.ServerLoginPacketListenerImpl",
            "net.minecraft.server.network.ServerGamePacketListenerImpl",
            "net.minecraft.server.level.ServerPlayerGameMode",
            "net.minecraft.server.PlayerAdvancements",
            "net.minecraft.world.entity.player.Player",
            "net.minecraft.world.food.FoodData",
            "net.minecraft.world.inventory.MerchantResultSlot",
            "net.minecraft.world.entity.npc.AbstractVillager",
            "net.minecraft.network.protocol.game.ClientboundOpenScreenPacket",
    };

    /** CraftBukkit 侧：包名带版本号，运行时按 {@code CraftServer} 所在包拼。 */
    private static final String[] CRAFT_CLASSES = {
            "entity.CraftHumanEntity",
            "generator.CraftWorldInfo",
            "tag.CraftTag",
    };

    /**
     * 逐个 {@code Class.forName}，返回一行可读摘要（加载成功的简名 + 失败项）。
     * 失败不抛：这里只负责"把类拉进变换范围"，真正的判定交给 mixin 自己的
     * {@code countInjections}（锚点失效会在变换时直接抛 InvalidInjectionException）。
     */
    public static String loadAll() {
        List<String> loaded = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (String name : NMS_CLASSES) {
            load(name, loaded, failed);
        }
        String craftPackage = craftPackage();
        if (craftPackage != null) {
            for (String suffix : CRAFT_CLASSES) {
                load(craftPackage + '.' + suffix, loaded, failed);
            }
        } else {
            failed.add("craftbukkit package unresolved");
        }
        return "loaded=" + String.join(",", loaded)
                + (failed.isEmpty() ? "" : " failed=" + String.join(",", failed));
    }

    private static void load(String name, List<String> loaded, List<String> failed) {
        try {
            Class<?> c = Class.forName(name, false, PaperarcInjectionCoverage.class.getClassLoader());
            // initialize=false 只做定义+变换，不跑 <clinit>，避免提前触发别的初始化
            loaded.add(c.getSimpleName());
        } catch (Throwable t) {
            failed.add(name + '(' + t.getClass().getSimpleName() + ')');
        }
    }

    private static String craftPackage() {
        try {
            return org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
        } catch (Throwable t) {
            return null;
        }
    }
}
