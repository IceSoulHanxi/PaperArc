package dev.paperarc.mixin.common.entity;

import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires Paper's {@link EntityMoveEvent} for non-player living entities at the
 * tail of {@link LivingEntity#tick}, mirroring the Paper patch (which sits
 * after {@code pushEntities} inside tick). Position/rotation at the head of
 * the tick is captured into per-entity unique fields instead of shadowing the
 * parent-class {@code xo/yo/zo/yRotO/xRotO} fields.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMoveEventMixin {

    @Unique private double paperarc$moveFromX;
    @Unique private double paperarc$moveFromY;
    @Unique private double paperarc$moveFromZ;
    @Unique private float paperarc$moveFromYRot;
    @Unique private float paperarc$moveFromXRot;

    @Inject(method = "tick", at = @At("HEAD"))
    private void paperarc$capturePreTickPose(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        this.paperarc$moveFromX = self.getX();
        this.paperarc$moveFromY = self.getY();
        this.paperarc$moveFromZ = self.getZ();
        this.paperarc$moveFromYRot = self.getYRot();
        this.paperarc$moveFromXRot = self.getXRot();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void paperarc$fireMoveEvent(CallbackInfo ci) {
        final LivingEntity self = (LivingEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!((ServerLevelMoveEventBridge) serverLevel).paperarc$hasEntityMoveEvent()) {
            return;
        }
        if (self instanceof net.minecraft.world.entity.player.Player) {
            return;
        }
        if (this.paperarc$moveFromX == self.getX() && this.paperarc$moveFromY == self.getY()
            && this.paperarc$moveFromZ == self.getZ() && this.paperarc$moveFromYRot == self.getYRot()
            && this.paperarc$moveFromXRot == self.getXRot()) {
            return;
        }
        final Location from = new Location(PaperArcBridge.bukkitWorld(serverLevel),
            this.paperarc$moveFromX, this.paperarc$moveFromY, this.paperarc$moveFromZ,
            this.paperarc$moveFromYRot, this.paperarc$moveFromXRot);
        final Location to = new Location(PaperArcBridge.bukkitWorld(serverLevel),
            self.getX(), self.getY(), self.getZ(), self.getYRot(), self.getXRot());
        final EntityMoveEvent event = new EntityMoveEvent(
            (org.bukkit.entity.LivingEntity) PaperArcBridge.bukkitEntity(self), from, to.clone());
        if (!event.callEvent()) {
            self.absMoveTo(from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch());
        } else if (!to.equals(event.getTo())) {
            self.absMoveTo(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ(),
                event.getTo().getYaw(), event.getTo().getPitch());
        }
    }
}
