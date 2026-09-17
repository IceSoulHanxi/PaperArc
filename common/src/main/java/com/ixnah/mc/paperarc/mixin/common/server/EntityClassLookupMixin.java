package com.ixnah.mc.paperarc.mixin.common.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

/**
 * 让 Arclight 的实体类型自检忽略"不是 Bukkit 实体类型"的接口（B3-2 真机踩出来的）。
 *
 * <p>{@code EntityClassLookup.init()} 会把所有能从 Craft 实体类走到的、
 * {@code org.bukkit.entity.Entity} 的子接口收集起来，逐个要求有 NMS 映射，没有就
 * {@code RuntimeException: Missing valid entity class mapping} —— 服务器直接起不来。
 * 它只留了一个写死的 {@code ignored} 集合（Boss/Vehicle/Enemy 这类"能力接口"）。
 *
 * <p>paper 把一批能力拆成了 {@code io.papermc.paper.entity.Shearable}、
 * {@code com.destroystokyo.paper.entity.RangedEntity} 这样的小接口，它们都
 * {@code extends org.bukkit.entity.Entity}；我们按 paper 给运行时接口补上这些父接口后，
 * 它们（以及 Mixin 会顺带挂到目标上的 IfaceMixin 接口本身）就全都撞上了这个自检。
 *
 * <p>判据用包名：Arclight 这段自检本来就只对 Bukkit 自己的实体类型接口有意义，
 * 不在 {@code org.bukkit.entity.} 下的一律当成能力接口跳过。
 * 只拦 {@code init} 里那一次 {@code Set#contains}（另外两次 contains 的接收者是
 * {@code HashSet}，描述符不同，不会误伤）。
 */
@Mixin(targets = "io.izzel.arclight.common.mod.server.entity.EntityClassLookup", remap = false)
public abstract class EntityClassLookupMixin {

    @WrapOperation(method = "init", remap = false,
            at = @At(value = "INVOKE", remap = false,
                     target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private static boolean paperarc$ignoreNonBukkitEntityInterfaces(
            Set<Object> ignored, Object entityClass, Operation<Boolean> original) {
        if (entityClass instanceof Class<?> type && !type.getName().startsWith("org.bukkit.entity.")) {
            return true;
        }
        return original.call(ignored, entityClass);
    }
}
