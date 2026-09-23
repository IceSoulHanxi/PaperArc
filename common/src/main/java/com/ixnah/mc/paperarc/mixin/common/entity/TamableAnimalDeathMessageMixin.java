package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.papermc.paper.event.entity.TameableDeathMessageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.craftbukkit.v.util.CraftChatMessage.ChatSerializer;
// alias: ChatSerializer
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper 1.20.1's Add-TameableDeathMessageEvent.patch.
 *
 * <p>Paper replaces the {@code sendSystemMessage(getDeathMessage())} inside
 * {@code TamableAnimal#die} with a fire of {@link TameableDeathMessageEvent}; cancelling
 * suppresses the message, mutating {@code deathMessage(...)} changes what the owner sees.
 * We wrap the {@code sendSystemMessage} INVOKE (vanilla {@code die} bytecode offset 47).
 *
 * <p>1.20.1 paper-api ships no {@code PaperAdventure}, so the NMS {@code Component} is
 * bridged through GSON via {@link GsonComponentSerializer} (matching the rest of this
 * codebase; {@code ChatSerializer.toJson/fromJson} are the 1-arg mojmap forms here).
 */
@Mixin(TamableAnimal.class)
public abstract class TamableAnimalDeathMessageMixin {

    @WrapOperation(
            method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void paperarc$tameableDeathMessage(net.minecraft.server.level.ServerPlayer owner,
                                               net.minecraft.network.chat.Component vanillaMsg,
                                               Operation<Void> original) {
        TamableAnimal self = (TamableAnimal) (Object) this;
        // 1.21.1 的 Component.ChatSerializer 需要 HolderLookup.Provider（1.20.1 是单参）
        net.minecraft.core.HolderLookup.Provider registries = self.registryAccess();
        Component adventureMsg = GsonComponentSerializer.gson()
                .deserialize(ChatSerializer.toJson(vanillaMsg, registries));
        TameableDeathMessageEvent event = new TameableDeathMessageEvent(
                (org.bukkit.entity.Tameable) PaperArcBridge.bukkitEntity(self), adventureMsg);
        if (event.callEvent()) {
            owner.sendSystemMessage(ChatSerializer.fromJson(
                    GsonComponentSerializer.gson().serialize(event.deathMessage()), registries));
        }
    }
}
