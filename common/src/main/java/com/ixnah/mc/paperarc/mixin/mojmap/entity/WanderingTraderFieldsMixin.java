package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.WanderingTraderBridge;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code WanderingTrader.canDrinkPotion}/{@code canDrinkMilk}
 * supplementary fields (Add-more-WanderingTrader-API.patch). Field names match
 * Paper exactly (no {@code paperarc$} prefix) for reflection ABI compatibility;
 * access methods carry the {@code paper$} prefix through
 * {@link WanderingTraderBridge} because Paper's patch adds no NMS accessor.
 */
@Mixin(WanderingTrader.class)
public abstract class WanderingTraderFieldsMixin implements WanderingTraderBridge {

    @Unique
    public boolean canDrinkPotion = true; // Paper

    @Unique
    public boolean canDrinkMilk = true; // Paper

    @Override
    public boolean paper$canDrinkPotion() {
        return this.canDrinkPotion;
    }

    @Override
    public void paper$setCanDrinkPotion(boolean canDrinkPotion) {
        this.canDrinkPotion = canDrinkPotion;
    }

    @Override
    public boolean paper$canDrinkMilk() {
        return this.canDrinkMilk;
    }

    @Override
    public void paper$setCanDrinkMilk(boolean canDrinkMilk) {
        this.canDrinkMilk = canDrinkMilk;
    }
}
