package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftEntityBridge;
import net.minecraft.world.entity.Shearable;
import org.bukkit.craftbukkit.v.entity.CraftMushroomCow;
import org.bukkit.craftbukkit.v.entity.CraftSheep;
import org.bukkit.craftbukkit.v.entity.CraftSnowman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code io.papermc.paper.entity.Shearable} 的终端方法（A4-3 父接口差集）。
 *
 * <p>paper-api 的 {@code Sheep}/{@code MushroomCow}/{@code Snowman} 都 extends
 * {@code Shearable}，对应的 NMS 实体都实现 {@code net.minecraft.world.entity.Shearable}。
 * 三个 Craft 类互不继承，用多目标 mixin 一次覆盖；句柄走 CraftEntity 上的 duck 接口
 * （多目标下不能 {@code extends} 某个具体父类）。</p>
 */
@Mixin({CraftSheep.class, CraftMushroomCow.class, CraftSnowman.class})
public abstract class CraftShearableApiMixin {

    @Unique
    private Shearable paperarc$shearable() {
        return (Shearable) ((CraftEntityBridge) (Object) this).paperarc$getHandle();
    }

    @Unique
    public boolean readyToBeSheared() {
        return this.paperarc$shearable().readyForShearing();
    }

    @Unique
    public void shear(net.kyori.adventure.sound.Sound.Source source) {
        // adventure Sound.Source 与 NMS SoundSource 的常量顺序完全一致（javap 核对）
        this.paperarc$shearable().shear(source == null
                ? net.minecraft.sounds.SoundSource.PLAYERS
                : net.minecraft.sounds.SoundSource.values()[source.ordinal()]);
    }
}
