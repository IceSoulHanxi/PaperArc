package com.ixnah.mc.paperarc.mixin.common.item;

import io.papermc.paper.event.player.PlayerNameEntityEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v.util.CraftChatMessage;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's Add-PlayerNameEntityEvent patch.
 * <p>
 * Injected right before {@code LivingEntity#setCustomName} inside
 * {@code NameTagItem#interactLivingEntity} (all guard conditions already
 * evaluated there); the handler fully replicates the remaining tail and
 * cancels, so the original rename never runs.
 * <p>
 * Paper uses PaperAdventure for Component conversion; here we round-trip
 * through JSON ({@link CraftChatMessage} + adventure-gson) instead.
 * Arclight has no mixin on NameTagItem.
 */
@Mixin(NameTagItem.class)
public class NameTagItemNameEntityMixin {

    @Inject(
        method = "interactLivingEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setCustomName(Lnet/minecraft/network/chat/Component;)V"
        ),
        cancellable = true
    )
    private void paperarc$onNameEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        // 1.20.1: no data components; custom name comes from the stack hover name
        PlayerNameEntityEvent event = new PlayerNameEntityEvent(
            PaperArcBridge.bukkitPlayer(user),
            PaperArcBridge.bukkitEntity(entity),
            GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(stack.getHoverName())),
            true
        );
        if (!event.callEvent()) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }
        LivingEntity newEntity = ((CraftLivingEntity) event.getEntity()).getHandle();
        newEntity.setCustomName(event.getName() != null ? CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(event.getName())) : null);
        if (event.isPersistent() && newEntity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        // vanilla tail: consume one name tag, sided success
        stack.shrink(1);
        cir.setReturnValue(InteractionResult.sidedSuccess(user.level().isClientSide));
    }
}
