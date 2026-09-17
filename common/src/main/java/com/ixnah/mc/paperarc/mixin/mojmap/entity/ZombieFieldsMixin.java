package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.ZombieBridge;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code Zombie.shouldBurnInDay} supplementary field
 * (Add-more-Zombie-API.patch). Field name matches Paper exactly (no
 * {@code paperarc$} prefix) so reflection on the NMS class is ABI-compatible
 * with Paper. Paper adds NMS accessor methods {@code shouldBurnInDay()} and
 * {@code setShouldBurnInDay(boolean)}, which the bridge exposes under those
 * names; Paper's {@code isSunSensitive()} also reads the field directly.
 */
@Mixin(Zombie.class)
public abstract class ZombieFieldsMixin implements ZombieBridge {

    @Unique
    public boolean shouldBurnInDay = true; // Paper

    @Override
    public boolean shouldBurnInDay() {
        return this.shouldBurnInDay;
    }

    @Override
    public void setShouldBurnInDay(boolean shouldBurnInDay) {
        this.shouldBurnInDay = shouldBurnInDay;
    }
}
