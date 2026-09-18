package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftEntityBridge;
import io.papermc.paper.entity.SchoolableFish;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import org.bukkit.craftbukkit.v.entity.CraftCod;
import org.bukkit.craftbukkit.v.entity.CraftSalmon;
import org.bukkit.craftbukkit.v.entity.CraftTropicalFish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code io.papermc.paper.entity.SchoolableFish} 的终端方法（A5-1 父接口差集）。
 *
 * <p>Paper 的做法是另开一个 {@code PaperSchoolableFish extends CraftFish} 并在实体转换表里
 * 替换掉三种鱼；我们改不了 Arclight 的转换表，改为把实现体挂在三个互不继承的 Craft 类上
 * （{@code AbstractSchoolingFish} 的子类只有 cod/salmon/tropical_fish）。</p>
 *
 * <p>{@code leader}/{@code schoolSize} 是 NMS private 字段，已由 AT 放开直访（srg 名
 * {@code f_27520_}/{@code f_27521_}）。{@code stopFollowing()} 在 vanilla 里不判空，
 * Paper 给 NMS 打了补丁，我们改成调用前自己判 {@code isFollower()}。</p>
 */
@Mixin({CraftCod.class, CraftSalmon.class, CraftTropicalFish.class})
public abstract class CraftSchoolableFishApiMixin {

    @Unique
    private AbstractSchoolingFish paperarc$schooling() {
        return (AbstractSchoolingFish) ((CraftEntityBridge) (Object) this).paperarc$getHandle();
    }

    @Unique
    public void startFollowing(SchoolableFish fish) {
        AbstractSchoolingFish handle = this.paperarc$schooling();
        // 已经在跟随别的鱼时要先正确退出，否则旧 leader 的 schoolSize 不会减回去（同 Paper）
        if (handle.isFollower()) {
            handle.stopFollowing();
        }
        handle.startFollowing((AbstractSchoolingFish)
                ((CraftEntityBridge) (Object) fish).paperarc$getHandle());
    }

    @Unique
    public void stopFollowing() {
        AbstractSchoolingFish handle = this.paperarc$schooling();
        // vanilla 的 stopFollowing 不判空，leader 为 null 时直接 NPE（Paper 是改 NMS，我们在这里挡）
        if (handle.isFollower()) {
            handle.stopFollowing();
        }
    }

    @Unique
    public int getSchoolSize() {
        return this.paperarc$schooling().schoolSize;
    }

    @Unique
    public int getMaxSchoolSize() {
        return this.paperarc$schooling().getMaxSchoolSize();
    }

    @Unique
    public SchoolableFish getSchoolLeader() {
        AbstractSchoolingFish leader = this.paperarc$schooling().leader;
        if (leader == null) {
            return null;
        }
        // Paper 这里走 NMS 的 getBukkitEntity()（编译期不可见），我们走运行时自己的实体查找。
        // 注意：Arclight 上 Bukkit 实体包装对象**没有身份唯一性** —— 探针 P12b 实测
        // world.getEntity(uuid) 与 world.spawn(...) 返回的不是同一个对象，
        // 所以任何返回实体的 paper API 都不能指望插件用 == 比较（要判 UUID）。
        org.bukkit.entity.Entity cached = org.bukkit.Bukkit.getEntity(leader.getUUID());
        return cached instanceof SchoolableFish fish ? fish
                : PaperArcBridge.<SchoolableFish>bukkitEntity(leader);
    }
}
