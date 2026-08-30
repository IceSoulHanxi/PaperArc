package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code WanderingTrader.canDrinkPotion} /
 * {@code canDrinkMilk} supplementary fields to the api mixins. Paper's patch
 * adds the fields without NMS accessor methods, so the bridge methods carry the
 * {@code paper$} prefix.
 */
public interface WanderingTraderBridge {

    boolean paper$canDrinkPotion();

    void paper$setCanDrinkPotion(boolean canDrinkPotion);

    boolean paper$canDrinkMilk();

    void paper$setCanDrinkMilk(boolean canDrinkMilk);
}
