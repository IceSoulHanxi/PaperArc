package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ZombieBridge;
import net.minecraft.world.entity.monster.Zombie;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftZombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Add-more-Zombie-API / Zombie-API-breaking-doors additions on
 * {@link CraftZombie}.
 *
 * <p>Mappings to this codebase's NMS (mojmap 1.20.1 {@code Zombie}):
 * <ul>
 *   <li>{@code isDrowning()} → public NMS {@code Zombie#isUnderWaterConverting()}.</li>
 *   <li>{@code startDrowning(int)} / {@code stopDrowning()} → vanilla state
 *       change through the access-widened {@code conversionTime} counter and the
 *       {@code DATA_DROWNED_CONVERSION_ID} synched flag.</li>
 *   <li>{@code isArmsRaised()} / {@code setArmsRaised(boolean)} → vanilla
 *       {@code Zombie#isAggressive()} / {@code setAggressive(boolean)}
 *       (Paper delegates the same way — no extra field).</li>
 *   <li>{@code shouldBurnInDay()} / {@code setShouldBurnInDay(boolean)} →
 *       Paper stores the flag in an NMS {@code Zombie.shouldBurnInDay} field
 *       injected by {@code ZombieFieldsMixin} and reached through
 *       {@link ZombieBridge} (Paper adds NMS accessors under those names).
 *       {@code shouldBurnInDay()} falls back to {@code isSunSensitive()} when
 *       unset, mirroring vanilla semantics.</li>
 *   <li>{@code supportsBreakingDoors()} → protected NMS
 *       {@code Zombie#supportsBreakDoorGoal()} via AT.</li>
 * </ul>
 */
@Mixin(CraftZombie.class)
public abstract class CraftZombieApiMixin {

    @Shadow
    public abstract Zombie getHandle();

    @Unique
    public boolean isArmsRaised() {
        return this.getHandle().isAggressive();
    }

    @Unique
    public boolean isDrowning() {
        return this.getHandle().isUnderWaterConverting();
    }

    @Unique
    public void setArmsRaised(boolean armsRaised) {
        this.getHandle().setAggressive(armsRaised);
    }

    @Unique
    public void setShouldBurnInDay(boolean shouldBurnInDay) {
        ((ZombieBridge) this.getHandle()).setShouldBurnInDay(shouldBurnInDay);
    }

    @Unique
    public boolean shouldBurnInDay() {
        // 无值时按 vanilla 公式取默认：普通僵尸/尸壳等由 isSunSensitive() 决定是否日晒燃烧
        ZombieBridge bridge = (ZombieBridge) this.getHandle();
        return bridge.shouldBurnInDay() ? true : this.getHandle().isSunSensitive();
    }

    @Unique
    public void startDrowning(int drownedConversionTime) {
        // 等价私有 vanilla Zombie#startUnderWaterConversion(int)：置转换倒计时并打开同步位
        Zombie handle = this.getHandle();
        handle.conversionTime = drownedConversionTime;
        handle.getEntityData().set(Zombie.DATA_DROWNED_CONVERSION_ID, true);
    }

    @Unique
    public void stopDrowning() {
        // vanilla 1.20.1 没有 stop 方法：清掉倒计时并关闭同步位（isUnderWaterConverting() 即转 false）
        Zombie handle = this.getHandle();
        handle.conversionTime = -1;
        handle.getEntityData().set(Zombie.DATA_DROWNED_CONVERSION_ID, false);
    }

    @Unique
    public boolean supportsBreakingDoors() {
        return this.getHandle().supportsBreakDoorGoal();
    }
}
