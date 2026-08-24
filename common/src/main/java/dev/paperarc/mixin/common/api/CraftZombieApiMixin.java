package dev.paperarc.mixin.common.api;

import dev.paperarc.bridge.ApiState;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Zombie;
import org.bukkit.craftbukkit.v.entity.CraftZombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Method;

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
 *       through the shadowed {@code conversionTime} counter and the
 *       {@code DATA_DROWNED_CONVERSION_ID} synched flag (vanilla 1.21.1 has no
 *       public/private "stop" method).</li>
 *   <li>{@code isArmsRaised()} / {@code setArmsRaised(boolean)} and
 *       {@code shouldBurnInDay()} / {@code setShouldBurnInDay(boolean)} → Paper
 *       stores these in NMS-patched fields that vanilla 1.21.1 {@code Zombie}
 *       has no storage for (arms-raised is not a synced property, sun-burning is
 *       hard-gated by {@code isSunSensitive()}), so they are kept in
 *       {@link ApiState} keyed by the NMS handle (side-map). {@code
 *       shouldBurnInDay()} defaults to the vanilla formula value of the handle's
 *       {@code isSunSensitive()}.</li>
 *   <li>{@code supportsBreakingDoors()} → protected NMS
 *       {@code Zombie#supportsBreakDoorGoal()} (@Shadow).</li>
 * </ul>
 */
@Mixin(CraftZombie.class)
public abstract class CraftZombieApiMixin {

    @Shadow
    public abstract Zombie getHandle();

    @Shadow
    protected abstract boolean supportsBreakDoorGoal();

    @Shadow
    protected int conversionTime;

    @Shadow
    private static EntityDataAccessor<Boolean> DATA_DROWNED_CONVERSION_ID;

    @Unique
    private static final String PAPERARC$KEY_ARMS_RAISED = "zombie$armsRaised";

    @Unique
    private static final String PAPERARC$KEY_SHOULD_BURN_IN_DAY = "zombie$shouldBurnInDay";

    /**
     * Cached reflective handle to protected vanilla {@code Zombie#isSunSensitive()},
 * used as the default value of {@link #shouldBurnInDay()}.
     */
    @Unique
    private static volatile Method PAPERARC$IS_SUN_SENSITIVE;

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
            paperarc$isSunSensitive(this.getHandle()));
    }

    @Unique
    public void startDrowning(int drownedConversionTime) {
        // 等价私有 vanilla Zombie#startUnderWaterConversion(int)：置转换倒计时并打开同步位
        this.conversionTime = drownedConversionTime;
        this.getHandle().getEntityData().set(DATA_DROWNED_CONVERSION_ID, true);
    }

    @Unique
    public void stopDrowning() {
        // vanilla 1.21.1 没有 stop 方法：清掉倒计时并关闭同步位（isUnderWaterConverting() 即转 false）
        this.conversionTime = -1;
        this.getHandle().getEntityData().set(DATA_DROWNED_CONVERSION_ID, false);
    }

    @Unique
    public boolean supportsBreakingDoors() {
        return this.supportsBreakDoorGoal();
    }

    /**
     * Resolves protected vanilla {@code Zombie#isSunSensitive()} once.
     */
    @Unique
    private static boolean paperarc$isSunSensitive(Zombie handle) {
        try {
            Method method = PAPERARC$IS_SUN_SENSITIVE;
            if (method == null) {
                synchronized (CraftZombieApiMixin.class) {
                    if (PAPERARC$IS_SUN_SENSITIVE == null) {
                        Method sunSensitive = Zombie.class.getDeclaredMethod("isSunSensitive");
                        sunSensitive.setAccessible(true);
                        PAPERARC$IS_SUN_SENSITIVE = sunSensitive;
                    }
                    method = PAPERARC$IS_SUN_SENSITIVE;
                }
            }
            return (Boolean) method.invoke(handle);
        } catch (ReflectiveOperationException e) {
            // 普通僵尸默认日晒燃烧
            return true;
        }
    }
}
