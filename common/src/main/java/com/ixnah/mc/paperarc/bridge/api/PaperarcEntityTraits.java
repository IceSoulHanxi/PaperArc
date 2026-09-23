package com.ixnah.mc.paperarc.bridge.api;

import com.ixnah.mc.paperarc.bridge.ItemEntityBridge;
import com.ixnah.mc.paperarc.bridge.LivingEntityFieldsBridge;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.util.TriState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;

/**
 * paper 抽出来的那批"能力小接口"（{@code RangedEntity}/{@code Shearable}/…）的实现体。
 *
 * <p>这些接口 paper 是用 {@code CraftRangedEntity}/{@code PaperShearable} 这种
 * "带 default 方法体的中间接口"混进 Craft 类的；我们没法给运行时 Craft 类加父接口，
 * 改为在对应的 {@code *IfaceMixin} 上写 default 方法体、转调这里的静态实现。
 *
 * <p>方法体放这里而不是直接写在 mixin 里，是因为 mixin 内的辅助方法合并后描述符仍指向
 * mixin 类（见 {@code docs/mixin-conventions.md}）；顶层 bridge 类没有这个问题。
 * 参数一律收成 {@code Object}，由调用方（接口 default 方法里的 {@code this}）保证类型。
 */
public final class PaperarcEntityTraits {

    private PaperarcEntityTraits() {
    }

    private static net.minecraft.world.entity.Entity handle(Object self) {
        return ((CraftEntity) self).getHandle();
    }

    // ---------------------------------------------- com.destroystokyo.paper.entity.RangedEntity

    /** paper {@code CraftRangedEntity#rangedAttack}。 */
    public static void rangedAttack(Object self, org.bukkit.entity.LivingEntity target, float charge) {
        ((RangedAttackMob) handle(self)).performRangedAttack(((CraftLivingEntity) target).getHandle(), charge);
    }

    /** paper {@code CraftRangedEntity#setChargingAttack}：vanilla 用 aggressive 位表示"举手"。 */
    public static void setChargingAttack(Object self, boolean raiseHands) {
        ((Mob) handle(self)).setAggressive(raiseHands);
    }

    // ---------------------------------------------- io.papermc.paper.entity.Frictional

    /**
     * paper {@code CraftLivingEntity/CraftItem#getFrictionState}。
     *
     * <p>Paper 在 {@code LivingEntity} 与 {@code ItemEntity} 上各加了一个
     * {@code frictionState} 字段，这里由 {@code entity.*FieldsMixin} 注入、经 duck
     * 接口读写；{@code org.bukkit.entity.Item} 与 {@code LivingEntity} 是两套宿主，
     * 用 instanceof 分流。
     */
    public static TriState getFrictionState(Object self) {
        net.minecraft.world.entity.Entity handle = handle(self);
        return handle instanceof net.minecraft.world.entity.LivingEntity
                ? ((LivingEntityFieldsBridge) handle).paper$getFrictionState()
                : ((ItemEntityBridge) handle).paper$getFrictionState();
    }

    public static void setFrictionState(Object self, TriState state) {
        net.minecraft.world.entity.Entity handle = handle(self);
        if (handle instanceof net.minecraft.world.entity.LivingEntity) {
            ((LivingEntityFieldsBridge) handle).paper$setFrictionState(state);
        } else {
            ((ItemEntityBridge) handle).paper$setFrictionState(state);
        }
    }

    // ---------------------------------------------- io.papermc.paper.entity.Shearable

    /** paper {@code PaperShearable#readyToBeSheared}。 */
    public static boolean readyToBeSheared(Object self) {
        return ((net.minecraft.world.entity.Shearable) handle(self)).readyForShearing();
    }

    /** paper {@code PaperShearable#shear}。 */
    public static void shear(Object self, Sound.Source source) {
        net.minecraft.world.entity.Entity entity = handle(self);
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            ((net.minecraft.world.entity.Shearable) entity).shear(serverLevel, asVanilla(source), net.minecraft.world.item.ItemStack.EMPTY);
        }
    }

    /**
     * adventure 的 {@code Sound.Source} → NMS {@code SoundSource}（paper 走
     * {@code PaperAdventure.asVanilla}，Arclight 没有那个类）。
     * 两个枚举的顺序一致但有三个名字不同（RECORD/BLOCK/PLAYER ↔ RECORDS/BLOCKS/PLAYERS），
     * 所以写成显式 switch 而不是 {@code valueOf(name())} 或按 ordinal 取。
     */
    private static SoundSource asVanilla(Sound.Source source) {
        return switch (source) {
            case MASTER -> SoundSource.MASTER;
            case MUSIC -> SoundSource.MUSIC;
            case RECORD -> SoundSource.RECORDS;
            case WEATHER -> SoundSource.WEATHER;
            case BLOCK -> SoundSource.BLOCKS;
            case HOSTILE -> SoundSource.HOSTILE;
            case NEUTRAL -> SoundSource.NEUTRAL;
            case PLAYER -> SoundSource.PLAYERS;
            case AMBIENT -> SoundSource.AMBIENT;
            case VOICE -> SoundSource.VOICE;
            case UI -> SoundSource.UI;
        };
    }

    // ---------------------------------------------- io.papermc.paper.entity.Bucketable

    public static boolean isFromBucket(Object self) {
        return ((net.minecraft.world.entity.animal.Bucketable) handle(self)).fromBucket();
    }

    public static void setFromBucket(Object self, boolean fromBucket) {
        ((net.minecraft.world.entity.animal.Bucketable) handle(self)).setFromBucket(fromBucket);
    }

    public static org.bukkit.inventory.ItemStack getBaseBucketItem(Object self) {
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(
                ((net.minecraft.world.entity.animal.Bucketable) handle(self)).getBucketItemStack());
    }

    public static org.bukkit.Sound getPickupSound(Object self) {
        return org.bukkit.craftbukkit.v.CraftSound.minecraftToBukkit(
                ((net.minecraft.world.entity.animal.Bucketable) handle(self)).getPickupSound());
    }

    // ---------------------------------------------- io.papermc.paper.entity.SchoolableFish

    private static net.minecraft.world.entity.animal.fish.AbstractSchoolingFish school(Object self) {
        return (net.minecraft.world.entity.animal.fish.AbstractSchoolingFish) handle(self);
    }

    public static void startFollowing(Object self, io.papermc.paper.entity.SchoolableFish leader) {
        school(self).startFollowing(school(leader));
    }

    public static void stopFollowing(Object self) {
        school(self).stopFollowing();
    }

    /** vanilla 的 {@code schoolSize} 是"这条鱼带着的跟随者数量"，领队身上才有意义。 */
    public static int getSchoolSize(Object self) {
        return school(self).schoolSize;
    }

    public static int getMaxSchoolSize(Object self) {
        return school(self).getMaxSchoolSize();
    }

    public static io.papermc.paper.entity.SchoolableFish getSchoolLeader(Object self) {
        net.minecraft.world.entity.animal.fish.AbstractSchoolingFish leader = school(self).leader;
        return leader == null ? null
                : com.ixnah.mc.paperarc.bridge.PaperArcBridge.<io.papermc.paper.entity.SchoolableFish>bukkitEntity(leader);
    }

    // ---------------------------------------------- io.papermc.paper.entity.Leashable

    public static boolean isLeashed(Object self) {
        return ((net.minecraft.world.entity.Leashable) handle(self)).isLeashed();
    }

    /** 与 {@code CraftLivingEntity#getLeashHolder} 一致：没拴绳时抛 IllegalStateException。 */
    public static org.bukkit.entity.Entity getLeashHolder(Object self) {
        net.minecraft.world.entity.Leashable leashable = (net.minecraft.world.entity.Leashable) handle(self);
        if (!leashable.isLeashed()) {
            throw new IllegalStateException("Entity not leashed");
        }
        return com.ixnah.mc.paperarc.bridge.PaperArcBridge.bukkitEntity(leashable.getLeashHolder());
    }

    public static boolean setLeashHolder(Object self, org.bukkit.entity.Entity holder) {
        net.minecraft.world.entity.Leashable leashable = (net.minecraft.world.entity.Leashable) handle(self);
        if (holder == null) {
            leashable.removeLeash();
            return true;
        }
        if (holder.isDead()) {
            return false;
        }
        leashable.setLeashedTo(((CraftEntity) holder).getHandle(), true);
        return true;
    }
}
