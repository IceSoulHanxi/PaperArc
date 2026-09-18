package com.ixnah.mc.paperarc.mixin.common.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

/**
 * 让 Arclight 的实体类型自检忽略"不是 Bukkit 实体类型"的接口（main 的 B3-2 真机踩出来，A5-1 移植）。
 *
 * <p>{@code EntityClassLookup.init()} 会从每个 {@code EntityType} 的 Bukkit 类出发，沿
 * {@code getInterfaces()} 收集所有「是 {@code org.bukkit.entity.Entity} 子类型」的接口，
 * 逐个要求在 {@code NMS_TO_BUKKIT} 里有 Craft 映射，没有就
 * {@code RuntimeException: Missing valid entity class mapping} —— 服务器直接起不来。
 * 它只留了一个写死的 {@code ignored} 集合（Explosive/Damageable/NPC/Boss/Breedable/
 * Steerable/Enemy/ComplexLivingEntity/Vehicle 这类"能力接口"）。
 *
 * <p>paper 把一批能力拆成了 {@code io.papermc.paper.entity.Shearable}、
 * {@code com.destroystokyo.paper.entity.RangedEntity} 这样的小接口，它们都
 * {@code extends org.bukkit.entity.Entity}；我们按 paper 给运行时接口补上这些父接口后，
 * 它们（以及 Mixin 会顺带挂到目标上的 IfaceMixin 接口本身）就全都撞上了这个自检。
 *
 * <p>判据用包名：Arclight 这段自检本来就只对 Bukkit 自己的实体类型接口有意义，
 * 不在 {@code org.bukkit.entity.} 下的一律当成能力接口跳过。只拦 {@code init} 里那一次
 * {@code Set#contains}（另外两次 contains 的接收者是 {@code HashSet}，描述符不同，不会误伤；
 * 1.20.1 运行时 jar javap 核对过只有一条 {@code InterfaceMethod java/util/Set.contains}）。
 *
 * <p>风险：第三方 mod 注册实体时它的 Bukkit 侧接口一定在 {@code org.bukkit.entity.} 下
 * （{@code EntityClass.bukkitClass} 的类型就是 {@code Class<? extends org.bukkit.entity.Entity>}），
 * 自检对它们照常生效；被放宽的只有"mod 自定义包外实体接口且忘了登记映射"这一种。
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
