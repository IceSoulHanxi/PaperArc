package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.WitherBossBridge;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code WitherBoss.canPortal} supplementary field
 * (Missing-Entity-API.patch). Field name matches Paper exactly (no
 * {@code paperarc$} prefix) so reflection on the NMS class is ABI-compatible
 * with Paper. Paper adds a NMS setter {@code setCanTravelThroughPortals(boolean)}
 * but no getter, so the bridge exposes the setter under the Paper name and the
 * getter with the {@code paper$} prefix.
 */
@Mixin(WitherBoss.class)
public abstract class WitherBossFieldsMixin implements WitherBossBridge {

    @Unique
    public boolean canPortal = false; // Paper

    @Override
    public boolean paper$canPortal() {
        return this.canPortal;
    }

    @Override
    public void setCanTravelThroughPortals(boolean canPortal) {
        this.canPortal = canPortal;
    }
}
