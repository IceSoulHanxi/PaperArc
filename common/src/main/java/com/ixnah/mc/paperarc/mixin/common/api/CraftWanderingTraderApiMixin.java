package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftWanderingTrader;
import org.bukkit.craftbukkit.v.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.ixnah.mc.paperarc.bridge.WanderingTraderBridge;

import java.lang.reflect.Field;

/**
 * Adds Paper's Add-more-WanderingTrader-API methods.
 *
 * getWanderingTowards/setWanderingTowards map onto vanilla {@code wanderTarget}
 * storage (private field widened via AT f_35840_ and read directly; the public
 * {@code setWanderTarget(BlockPos)} setter is called directly).
 * canDrinkMilk/canDrinkPotion are Paper fields injected into the NMS
 * {@code WanderingTrader} by {@code WanderingTraderFieldsMixin} and reached
 * through {@link com.ixnah.mc.paperarc.bridge.WanderingTraderBridge} (Paper's
 * initial value {@code true}).
 */
@Mixin(CraftWanderingTrader.class)
public abstract class CraftWanderingTraderApiMixin {

    @Shadow
    public abstract WanderingTrader getHandle();

    @Unique
    public boolean canDrinkMilk() {
        return ((WanderingTraderBridge) getHandle()).paper$canDrinkMilk();
    }

    @Unique
    public void setCanDrinkMilk(boolean bool) {
        ((WanderingTraderBridge) getHandle()).paper$setCanDrinkMilk(bool);
    }

    @Unique
    public boolean canDrinkPotion() {
        return ((WanderingTraderBridge) getHandle()).paper$canDrinkPotion();
    }

    @Unique
    public void setCanDrinkPotion(boolean bool) {
        ((WanderingTraderBridge) getHandle()).paper$setCanDrinkPotion(bool);
    }

    @Unique
    public Location getWanderingTowards() {
        BlockPos pos = getHandle().wanderTarget;
        if (pos == null) {
            return null;
        }
        return CraftLocation.toBukkit(pos, getHandle().level());
    }

    @Unique
    public void setWanderingTowards(Location location) {
        BlockPos pos = location != null ? CraftLocation.toBlockPosition(location) : null;
        getHandle().setWanderTarget(pos);
    }
}
