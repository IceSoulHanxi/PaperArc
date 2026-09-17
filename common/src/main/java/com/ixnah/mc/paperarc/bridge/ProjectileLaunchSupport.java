package com.ixnah.mc.paperarc.bridge;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;

/**
 * Shared implementation of Paper's {@code PlayerLaunchProjectileEvent}
 * (patches/server/PlayerLaunchProjectileEvent.patch) for the item mixins that
 * spawn a projectile from a player action.
 *
 * <p>The event's two outcomes are recorded in {@link LaunchState} so that the
 * later injection points inside the same {@code use()} / {@code useOn()} /
 * {@code releaseUsing()} invocation can suppress the item consumption and turn
 * the return value into Paper's FAIL/PASS.
 */
public final class ProjectileLaunchSupport {

    private ProjectileLaunchSupport() {
    }

    /**
     * Fires the event for {@code projectile}.
     *
     * @return true when the projectile may be spawned
     */
    public static boolean callLaunchEvent(Player user, ItemStack inHand, Entity projectile) {
        PlayerLaunchProjectileEvent event = new PlayerLaunchProjectileEvent(
                PaperArcBridge.bukkitPlayer(user),
                CraftItemStack.asCraftMirror(inHand),
                PaperArcBridge.bukkitEntity(projectile));
        boolean allowed = event.callEvent();
        LaunchState.cancelled(!allowed);
        LaunchState.noConsume(!event.shouldConsume());
        return allowed;
    }

    /** True when the item must not be consumed (cancelled launch or shouldConsume=false). */
    public static boolean suppressConsume() {
        return LaunchState.isCancelled() || LaunchState.isNoConsume();
    }

    /** Paper re-sends the inventory whenever the launch did not consume as vanilla would. */
    public static void updateInventory(Player user) {
        org.bukkit.entity.Player bukkit = PaperArcBridge.bukkitPlayer(user);
        if (bukkit != null) {
            bukkit.updateInventory();
        }
    }
}
