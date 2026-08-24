package dev.paperarc.mixin.common.entity;

import io.papermc.paper.event.entity.ElderGuardianAppearanceEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of Paper's ElderGuardianAppearanceEvent.
 * Paper threads a per-player {@code Predicate<ServerPlayer>} through a new
 * MobEffectUtil overload; the NMS jar here has no such overload, so we wrap
 * the single {@code addEffectToPlayersAround} call in
 * {@code ElderGuardian.customServerAiStep} and replicate the helper inline:
 * vanilla filter, then per-player event, then {@code addEffect} with a copied
 * instance (same as vanilla's lambda). The filtered list is returned so the
 * caller's GUARDIAN_ELDER_EFFECT packet forEach also skips cancelled players.
 */
@Mixin(ElderGuardian.class)
public abstract class ElderGuardianAppearanceMixin {

    @WrapOperation(
            method = "customServerAiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectUtil;addEffectToPlayersAround(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;DLnet/minecraft/world/effect/MobEffectInstance;I)Ljava/util/List;")
    )
    private List<ServerPlayer> paperarc$appearanceEvent(ServerLevel level, Entity entity, Vec3 pos, double dist,
                                                        MobEffectInstance effect, int duration,
                                                        Operation<List<ServerPlayer>> original) {
        Holder<MobEffect> holder = effect.getEffect();
        List<ServerPlayer> list = level.getPlayers(player ->
                player.gameMode.isSurvival()
                        && (entity == null || !entity.isAlliedTo(player))
                        && pos.closerThan(player.position(), dist)
                        && (!player.hasEffect(holder)
                            || player.getEffect(holder).getAmplifier() < effect.getAmplifier()
                            || player.getEffect(holder).endsWithin(duration - 1)));
        List<ServerPlayer> result = new ArrayList<>(list.size());
        for (ServerPlayer player : list) {
            ElderGuardianAppearanceEvent event = new ElderGuardianAppearanceEvent(
                    (org.bukkit.entity.ElderGuardian) PaperArcBridge.bukkitEntity((Entity) entity),
                    PaperArcBridge.bukkitPlayer(player));
            if (event.callEvent()) {
                result.add(player);
                player.addEffect(new MobEffectInstance(effect), entity);
            }
        }
        return result;
    }
}
