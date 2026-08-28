package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftWanderingTrader;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.ixnah.mc.paperarc.bridge.ApiState;

import java.lang.reflect.Field;

/**
 * Adds Paper's Add-more-WanderingTrader-API methods.
 *
 * getWanderingTowards/setWanderingTowards map onto vanilla {@code wanderTarget}
 * storage (private field read reflectively; the public
 * {@code setWanderTarget(BlockPos)} setter is called directly).
 * canDrinkMilk/canDrinkPotion have no vanilla NMS storage (Paper adds the
 * fields in its own patch), so they are kept in {@link ApiState} with Paper's
 * initial value {@code true} as the default.
 */
@Mixin(CraftWanderingTrader.class)
public abstract class CraftWanderingTraderApiMixin {

    @Unique
    private static final String PAPERARC$KEY_MILK = "wanderingTrader.canDrinkMilk";

    @Unique
    private static final String PAPERARC$KEY_POTION = "wanderingTrader.canDrinkPotion";

    @Unique
    private static volatile Field PAPERARC$WANDER_TARGET;

    @Shadow
    public abstract WanderingTrader getHandle();

    @Unique
    private static synchronized Field paperarc$resolveField() {
        if (PAPERARC$WANDER_TARGET == null) {
            try {
                Field resolved = WanderingTrader.class.getDeclaredField("wanderTarget");
                resolved.setAccessible(true);
                PAPERARC$WANDER_TARGET = resolved;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("NMS WanderingTrader.wanderTarget field not found", e);
            }
        }
        return PAPERARC$WANDER_TARGET;
    }

    @Unique
    public boolean canDrinkMilk() {
        return ApiState.get(getHandle(), PAPERARC$KEY_MILK, true);
    }

    @Unique
    public void setCanDrinkMilk(boolean bool) {
        ApiState.put(getHandle(), PAPERARC$KEY_MILK, bool);
    }

    @Unique
    public boolean canDrinkPotion() {
        return ApiState.get(getHandle(), PAPERARC$KEY_POTION, true);
    }

    @Unique
    public void setCanDrinkPotion(boolean bool) {
        ApiState.put(getHandle(), PAPERARC$KEY_POTION, bool);
    }

    @Unique
    public Location getWanderingTowards() {
        try {
            BlockPos pos = (BlockPos) paperarc$resolveField().get(getHandle());
            if (pos == null) {
                return null;
            }
            return CraftLocation.toBukkit(pos, getHandle().level());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS WanderingTrader.wanderTarget not accessible", e);
        }
    }

    @Unique
    public void setWanderingTowards(Location location) {
        BlockPos pos = location != null ? CraftLocation.toBlockPosition(location) : null;
        getHandle().setWanderTarget(pos);
    }
}
