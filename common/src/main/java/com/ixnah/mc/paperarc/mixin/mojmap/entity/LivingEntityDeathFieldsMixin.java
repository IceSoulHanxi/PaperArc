package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.DeathEventBridge;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** Paper 的 {@code LivingEntity.silentDeath} 与"待消费的 EntityDeathEvent"，见 {@link DeathEventBridge}。 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDeathFieldsMixin implements DeathEventBridge {

    @Shadow
    protected abstract SoundEvent getDeathSound();

    @Shadow
    protected abstract float getSoundVolume();

    @Unique
    public boolean silentDeath = false; // Paper

    @Unique
    private EntityDeathEvent paperarc$pendingDeathEvent;

    @Override
    public boolean paperarc$isSilentDeath() {
        return this.silentDeath;
    }

    @Override
    public void paperarc$setSilentDeath(boolean silent) {
        this.silentDeath = silent;
    }

    @Override
    public EntityDeathEvent paperarc$getPendingDeathEvent() {
        return this.paperarc$pendingDeathEvent;
    }

    @Override
    public void paperarc$setPendingDeathEvent(EntityDeathEvent event) {
        this.paperarc$pendingDeathEvent = event;
    }

    @Override
    public SoundEvent paperarc$getDeathSound() {
        return this.getDeathSound();
    }

    @Override
    public float paperarc$getDeathSoundVolume() {
        return this.getSoundVolume();
    }
}
