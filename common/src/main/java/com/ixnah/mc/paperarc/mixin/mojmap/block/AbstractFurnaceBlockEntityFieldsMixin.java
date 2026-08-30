package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.ixnah.mc.paperarc.bridge.AbstractFurnaceBlockEntityBridge;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code AbstractFurnaceBlockEntity.cookSpeedMultiplier}
 * supplementary field (Implement-furnace-cook-speed-multiplier-API.patch). Field
 * name matches Paper exactly (no {@code paperarc$} prefix) for reflection ABI
 * compatibility; access methods carry the {@code paper$} prefix through
 * {@link AbstractFurnaceBlockEntityBridge} because Paper's patch adds no NMS
 * accessor.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityFieldsMixin implements AbstractFurnaceBlockEntityBridge {

    @Unique
    public double cookSpeedMultiplier = 1.0D; // Paper

    @Override
    public double paper$getCookSpeedMultiplier() {
        return this.cookSpeedMultiplier;
    }

    @Override
    public void paper$setCookSpeedMultiplier(double multiplier) {
        this.cookSpeedMultiplier = multiplier;
    }
}
