package dev.paperarc.mixin.common.entity;

import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityFertilizeEggEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;

/**
 * Shared state/helper for the EntityFertilizeEggEvent port.
 * Replicates Paper's CraftEventFactory#callEntityFertilizeEggEvent minus the
 * bredWith item (Animal.breedItem is a CraftBukkit-added field that does not
 * exist in this compile-time NMS jar, so null is passed).
 */
final class FertilizeEggState {

    /**
     * Experience override for the next ExperienceOrb created by
     * Animal#finalizeSpawnChildFromBreeding, or null to keep the vanilla
     * random amount.
     */
    static final ThreadLocal<Integer> PENDING_EXPERIENCE = new ThreadLocal<>();

    private FertilizeEggState() {
    }

    /**
     * Fires {@link EntityFertilizeEggEvent}; on cancellation resets the love
     * of both animals so the breed pathfinding stops (Paper behavior).
     */
    static EntityFertilizeEggEvent call(Animal breeding, Animal other) {
        ServerPlayer breeder = breeding.getLoveCause();
        if (breeder == null) {
            breeder = other.getLoveCause();
        }
        int experience = breeding.getRandom().nextInt(7) + 1; // From Animal#spawnChildFromBreeding
        EntityFertilizeEggEvent event = new EntityFertilizeEggEvent(
                (org.bukkit.entity.LivingEntity) PaperArcBridge.bukkitEntity(breeding),
                (org.bukkit.entity.LivingEntity) PaperArcBridge.bukkitEntity(other),
                breeder == null ? null : PaperArcBridge.bukkitPlayer(breeder),
                null,
                experience);
        event.callEvent();
        if (event.isCancelled()) {
            breeding.resetLove();
            other.resetLove();
        }
        return event;
    }
}
