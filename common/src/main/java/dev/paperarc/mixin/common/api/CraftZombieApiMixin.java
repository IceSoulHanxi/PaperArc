package dev.paperarc.mixin.common.api;

import dev.paperarc.bridge.ApiState;
import net.minecraft.world.entity.monster.Zombie;
import org.bukkit.craftbukkit.v.entity.CraftZombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Add-more-Zombie-API / Zombie-API-breaking-doors additions on
 * {@link CraftZombie}.
 *
 * <p>Mappings to this codebase's NMS (mojmap 1.21.1 {@code Zombie}):
 * <ul>
 *   <li>{@code isDrowning()} → public NMS {@code Zombie#isUnderWaterConverting()}
 *       (reads the {@code DATA_DROWNED_CONVERSION_ID} synched flag).</li>
 *   <li>{@code startDrowning(int)} / {@code stopDrowning()} → replay the private
 *       vanilla {@code startUnderWaterConversion(int)} state change directly
 *       through the access-widened {@code conversionTime} counter and the
 *       {@code DATA_DROWNED_CONVERSION_ID} synched flag (vanilla 1.21.1 has no
 *       public/private "stop" method).</li>
 *   <li>{@code isArmsRaised()} / {@code setArmsRaised(boolean)} and
 *       {@code shouldBurnInDay()} / {@code setShouldBurnInDay(boolean)} → Paper
 *       stores these in NMS-patched fields that vanilla 1.21.1 {@code Zombie}
 *       has no storage for, so they are kept in {@link ApiState} keyed by the
 *       NMS handle (side-map). {@code shouldBurnInDay()} defaults to the
 *       widened {@code isSunSensitive()}.</li>
 *   <li>{@code supportsBreakingDoors()} → protected NMS
 *       {@code Zombie#supportsBreakDoorGoal()} via access widener.</li>
 * </ul>
 */
@Mixin(CraftZombie.class)
public abstract class CraftZombieApiMixin {

    @Shadow
    public abstract Zombie getHandle();

    @Unique
    private static final String PAPERARC$KEY_ARMS_RAISED = "zombie$armsRaised";

    @Unique
    private static final String PAPERARC$KEY_SHOULD_BURN_IN_DAY = "zombie$shouldBurnInDay";

    @Unique
    public boolean isArmsRaised() {
        return ApiState.get(this.getHandle(), PAPERARC$KEY_ARMS_RAISED, Boolean.FALSE);
    }

    @Unique
    public boolean isDrowning() {
        return this.getHandle().isUnderWaterConverting();
    }

    @Unique
    public void setArmsRaised(boolean armsRaised) {
        ApiState.put(this.getHandle(), PAPERARC$KEY_ARMS_RAISED, armsRaised);
    }

    @Unique
    public void setShouldBurnInDay(boolean shouldBurnInDay) {
        ApiState.put(this.getHandle(), PAPERARC$KEY_SHOULD_BURN_IN_DAY, shouldBurnInDay);
    }

    @Unique
    public boolean shouldBurnInDay() {
        // 无值时按 vanilla 公式取默认：普通僵尸/尸壳等由 isSunSensitive() 决定是否日晒燃烧
        return ApiState.get(this.getHandle(), PAPERARC$KEY_SHOULD_BURN_IN_DAY,
            this.getHandle().isSunSensitive());
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
        // vanilla 1.21.1 没有 stop 方法：清掉倒计时并关闭同步位（isUnderWaterConverting() 即转 false）
        Zombie handle = this.getHandle();
        handle.conversionTime = -1;
        handle.getEntityData().set(Zombie.DATA_DROWNED_CONVERSION_ID, false);
    }

    @Unique
    public boolean supportsBreakingDoors() {
        return this.getHandle().supportsBreakDoorGoal();
    }
}
