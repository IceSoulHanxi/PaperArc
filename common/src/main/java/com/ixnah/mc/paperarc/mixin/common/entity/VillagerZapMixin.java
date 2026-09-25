package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EntityZapEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.villager.Villager;
import org.bukkit.entity.LightningStrike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's Add-EntityZapEvent.patch for 1.21.11.
 *
 * <p>Paper fires {@link EntityZapEvent} in
 * {@code Villager#thunderHit(ServerLevel, LightningBolt)} when converting
 * to {@link Witch}; cancelling skips the conversion.
 */
@Mixin(Villager.class)
public abstract class VillagerZapMixin {

    @WrapOperation(
            method = "thunderHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/npc/villager/Villager;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"
            )
    )
    private Mob paperarc$fireZapEvent(Villager villager, EntityType<? extends Mob> entityType,
                                      ConversionParams conversionParams,
                                      ConversionParams.AfterConversion afterConversion,
                                      Operation<Mob> original,
                                      ServerLevel level, LightningBolt lightning) {
        Witch witch = EntityType.WITCH.create(level, EntitySpawnReason.CONVERSION);
        if (witch != null) {
            var event = new EntityZapEvent(
                    PaperArcBridge.bukkitEntity(villager),
                    (LightningStrike) PaperArcBridge.bukkitEntity(lightning),
                    PaperArcBridge.bukkitEntity(witch));
            if (!event.callEvent()) {
                witch.discard();
                return null;
            }
            witch.discard();
        }
        return original.call(villager, entityType, conversionParams, afterConversion);
    }
}
