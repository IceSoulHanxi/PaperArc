package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.AbstractSkeletonBridge;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code AbstractSkeleton.shouldBurnInDay} supplementary field
 * (Add-a-should-burn-in-sunlight-API-for-Phantoms-and-S.patch). Field name
 * matches Paper exactly (no {@code paperarc$} prefix) for reflection ABI
 * compatibility; access methods mirror Paper's NMS API surface through
 * {@link AbstractSkeletonBridge}.
 */
@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonFieldsMixin implements AbstractSkeletonBridge {

    @Unique
    private boolean shouldBurnInDay = true; // Paper

    @Override
    public boolean shouldBurnInDay() {
        return this.shouldBurnInDay;
    }

    @Override
    public void setShouldBurnInDay(boolean shouldBurnInDay) {
        this.shouldBurnInDay = shouldBurnInDay;
    }
}
