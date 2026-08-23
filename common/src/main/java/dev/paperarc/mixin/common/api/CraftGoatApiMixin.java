package dev.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.entity.CraftGoat;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.schedule.Activity;

/**
 * Adds ram(LivingEntity) missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Goat-ram-API.patch (NMS Goat#ram inlined here,
 * all Brain APIs are public so no NMS-side mixin needed).
 */
@Mixin(CraftGoat.class)
public abstract class CraftGoatApiMixin {

    @Shadow
    public abstract Goat getHandle();

    @Unique
    @SuppressWarnings("unchecked")
    public void ram(org.bukkit.entity.LivingEntity entity) {
        Goat handle = this.getHandle();
        net.minecraft.world.entity.LivingEntity target = ((CraftLivingEntity) entity).getHandle();
        Brain<Goat> brain = (Brain<Goat>) (Brain<?>) handle.getBrain();
        brain.setMemory(MemoryModuleType.RAM_TARGET, target.position());
        brain.eraseMemory(MemoryModuleType.RAM_COOLDOWN_TICKS);
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        brain.setActiveActivityIfPossible(Activity.RAM);
    }
}
