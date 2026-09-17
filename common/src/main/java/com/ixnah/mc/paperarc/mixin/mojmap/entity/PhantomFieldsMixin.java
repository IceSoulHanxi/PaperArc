package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.PhantomBridge;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code Phantom} supplementary fields
 * (Add-a-should-burn-in-sunlight-API-for-Phantoms-and-S.patch). Field names
 * match Paper exactly (no {@code paperarc$} prefix) for reflection ABI
 * compatibility; access methods mirror Paper's NMS API surface through
 * {@link PhantomBridge}.
 */
@Mixin(Phantom.class)
public abstract class PhantomFieldsMixin implements PhantomBridge {

    @Unique
    private boolean shouldBurnInDay = true; // Paper

    @Unique
    public java.util.UUID spawningEntity; // Paper

    @Override
    public boolean shouldBurnInDay() {
        return this.shouldBurnInDay;
    }

    @Override
    public void setShouldBurnInDay(boolean shouldBurnInDay) {
        this.shouldBurnInDay = shouldBurnInDay;
    }

    @Override
    public void setSpawningEntity(java.util.UUID entity) {
        this.spawningEntity = entity;
    }

    @Override
    public java.util.UUID paper$getSpawningEntity() {
        return this.spawningEntity;
    }
}
