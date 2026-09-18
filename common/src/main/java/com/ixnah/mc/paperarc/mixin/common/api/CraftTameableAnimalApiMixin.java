package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftEntityBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.bukkit.craftbukkit.v.entity.CraftAbstractHorse;
import org.bukkit.craftbukkit.v.entity.CraftTameableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code Tameable#getOwnerUniqueId()}（A5-3，pairing 基线 NO_IMPL）。
 *
 * <p>运行时的 {@code getOwner()} 要去拿 OfflinePlayer（可能触发 usercache 查询），
 * paper 单独加了只取 UUID 的这一条。{@code Tameable} 有两族互不继承的实现类：
 * {@code CraftTameableAnimal}（狼/猫/鹦鹉…，NMS {@code TamableAnimal}）与
 * {@code CraftAbstractHorse}（马/驴/骡/骆驼/羊驼…，NMS {@code AbstractHorse} 自带
 * 一套独立的 {@code owner} 字段），两个都要挂（pairing 的 PARTIAL_IMPL 判据实测）。</p>
 */
@Mixin({CraftTameableAnimal.class, CraftAbstractHorse.class})
public abstract class CraftTameableAnimalApiMixin {

    @Unique
    public java.util.UUID getOwnerUniqueId() {
        Entity handle = ((CraftEntityBridge) (Object) this).paperarc$getHandle();
        if (handle instanceof TamableAnimal tamable) {
            return tamable.getOwnerUUID();
        }
        return handle instanceof AbstractHorse horse ? horse.getOwnerUUID() : null;
    }
}
