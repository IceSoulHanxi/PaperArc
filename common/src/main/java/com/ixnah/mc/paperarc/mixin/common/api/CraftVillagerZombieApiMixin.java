package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.ZombieVillager;
import org.bukkit.craftbukkit.v.entity.CraftVillagerZombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's missing-entity-behaviour API {@code ZombieVillager.setConversionTime(int, boolean)}
 * to {@link CraftVillagerZombie}.
 *
 * <p>Vanilla 1.20.1 only has the 2-arg private {@code startConverting(UUID, int)}; the
 * Paper-patch 3-arg overload with {@code broadcastEntityEvent} is absent, so the boolean is
 * accepted for signature compatibility and the vanilla 2-arg path is used. Fields
 * ({@code villagerConversionTime} f_34365_, {@code conversionStarter} f_34360_,
 * {@code DATA_CONVERTING_ID} f_34359_) and {@code startConverting} (m_34383_) are widened
 * via AT.</p>
 */
@Mixin(CraftVillagerZombie.class)
public abstract class CraftVillagerZombieApiMixin {

    @Shadow
    public abstract ZombieVillager getHandle();

    @Unique
    public void setConversionTime(int time, boolean broadcastEntityEvent) {
        if (time < 0) {
            this.getHandle().villagerConversionTime = -1;
            this.getHandle().getEntityData().set(ZombieVillager.DATA_CONVERTING_ID, false);
            this.getHandle().conversionStarter = null;
            this.getHandle().removeEffect(MobEffects.DAMAGE_BOOST);
        } else {
            this.getHandle().startConverting(null, time);
        }
    }
}
