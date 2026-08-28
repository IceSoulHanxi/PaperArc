package com.ixnah.mc.paperarc.mixin.common.item;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerLaunchProjectileEvent for FireworkRocketItem.useOn
 * (PlayerLaunchProjectileEvent.patch — Paper 1.21.1 fires the event only in
 * useOn, i.e. right-clicking a block with a rocket; useOn cancel returns PASS).
 *
 * Vanilla 1.21.1 useOn: addFreshEntity then unconditional itemStack.shrink(1)
 * then sidedSuccess. Paper gates shrink with shouldConsume && !instabuild and
 * otherwise resyncs inventory (the inventory resync is omitted here — minor
 * documented deviation). Bytecode-verified: one addFreshEntity + one shrink
 * call site in useOn.
 */
@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {

    @WrapOperation(
            method = "useOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean paperarc$launch(Level world, Entity projectile, Operation<Boolean> original,
                                    UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide || context.getPlayer() == null) {
            return original.call(world, projectile);
        }
        ItemStack itemStack = context.getItemInHand();
        PlayerLaunchProjectileEvent event = new PlayerLaunchProjectileEvent(
                PaperArcBridge.bukkitPlayer(context.getPlayer()),
                CraftItemStack.asCraftMirror(itemStack),
                PaperArcBridge.bukkitEntity(projectile));
        boolean spawned = event.callEvent() && original.call(world, projectile);
        if (!spawned) {
            LaunchState.cancelled(true);
        } else {
            LaunchState.noConsume(!event.shouldConsume());
        }
        return spawned;
    }

    @WrapOperation(
            method = "useOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V")
    )
    private void paperarc$noShrink(ItemStack stack, int amount, Operation<Void> original, UseOnContext context) {
        if (!LaunchState.isCancelled() && !LaunchState.isNoConsume()) {
            original.call(stack, amount);
        }
    }

    @ModifyReturnValue(method = "useOn", at = @At("RETURN"))
    private InteractionResult paperarc$result(InteractionResult original, UseOnContext context) {
        return LaunchState.takeCancelled() ? InteractionResult.PASS : original;
    }
}
