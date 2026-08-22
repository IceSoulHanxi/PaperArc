package dev.paperarc.mixin.common.item;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.LaunchState;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerLaunchProjectileEvent for ExperienceBottleItem.use
 * (PlayerLaunchProjectileEvent.patch). Vanilla 1.21.1: create bottle,
 * shootFromRotation(-20°, 0.7F, 1.0F), addFreshEntity; tail is just the
 * sidedSuccess return. Cancel / spawn-failure -> fail(itemInHand), matching
 * Paper's early-return FAIL.
 */
@Mixin(ExperienceBottleItem.class)
public abstract class ExperienceBottleItemMixin {

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean paperarc$launch(Level world, Entity projectile, Operation<Boolean> original,
                                    Level level, Player user, InteractionHand hand) {
        if (world.isClientSide) {
            return original.call(world, projectile);
        }
        PlayerLaunchProjectileEvent event = new PlayerLaunchProjectileEvent(
                PaperArcBridge.bukkitPlayer(user),
                CraftItemStack.asCraftMirror(user.getItemInHand(hand)),
                PaperArcBridge.bukkitEntity(projectile));
        boolean spawned = event.callEvent() && original.call(world, projectile);
        LaunchState.cancelled(!spawned);
        return spawned;
    }

    @ModifyReturnValue(method = "use", at = @At("RETURN"))
    private InteractionResultHolder<ItemStack> paperarc$result(InteractionResultHolder<ItemStack> original,
                                                               Level level, Player user, InteractionHand hand) {
        return LaunchState.takeCancelled()
                ? InteractionResultHolder.fail(user.getItemInHand(hand))
                : original;
    }
}
