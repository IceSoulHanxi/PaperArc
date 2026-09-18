package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftEntityBridge;
import net.minecraft.world.entity.animal.Bucketable;
import org.bukkit.craftbukkit.v.CraftSound;
import org.bukkit.craftbukkit.v.entity.CraftAxolotl;
import org.bukkit.craftbukkit.v.entity.CraftFish;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code io.papermc.paper.entity.Bucketable} 的终端方法（A5-1 父接口差集）。
 *
 * <p>宿主与 Paper 的 {@code PaperBucketable} 完全一致：{@code CraftFish}（全部鱼类的公共基类）
 * 与 {@code CraftAxolotl}，两者互不继承，用多目标 mixin 一次覆盖；句柄走 CraftEntity 上的
 * duck 接口（多目标下不能 {@code extends} 某个具体父类）。</p>
 */
@Mixin({CraftFish.class, CraftAxolotl.class})
public abstract class CraftBucketableApiMixin {

    @Unique
    private Bucketable paperarc$bucketable() {
        return (Bucketable) ((CraftEntityBridge) (Object) this).paperarc$getHandle();
    }

    @Unique
    public boolean isFromBucket() {
        return this.paperarc$bucketable().fromBucket();
    }

    @Unique
    public void setFromBucket(boolean fromBucket) {
        this.paperarc$bucketable().setFromBucket(fromBucket);
    }

    @Unique
    public org.bukkit.inventory.ItemStack getBaseBucketItem() {
        return CraftItemStack.asBukkitCopy(this.paperarc$bucketable().getBucketItemStack());
    }

    @Unique
    public org.bukkit.Sound getPickupSound() {
        return CraftSound.getBukkit(this.paperarc$bucketable().getPickupSound());
    }
}
