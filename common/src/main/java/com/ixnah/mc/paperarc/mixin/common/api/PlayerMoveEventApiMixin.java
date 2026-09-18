package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code PlayerMoveEvent} 五个"变了吗"判定（A6/X-2 第六批）。
 * 全部由事件已有的 from/to 现算，逐行照 paper。
 */
@Mixin(PlayerMoveEvent.class)
public abstract class PlayerMoveEventApiMixin {

    @Unique
    private PlayerMoveEvent paperarc$self() {
        return (PlayerMoveEvent) (Object) this;
    }

    @Unique
    public boolean hasExplicitlyChangedPosition() {
        Location from = this.paperarc$self().getFrom();
        Location to = this.paperarc$self().getTo();
        return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
    }

    @Unique
    public boolean hasChangedPosition() {
        PlayerMoveEvent self = this.paperarc$self();
        return self.hasExplicitlyChangedPosition()
                || !self.getFrom().getWorld().equals(self.getTo().getWorld());
    }

    @Unique
    public boolean hasExplicitlyChangedBlock() {
        Location from = this.paperarc$self().getFrom();
        Location to = this.paperarc$self().getTo();
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }

    @Unique
    public boolean hasChangedBlock() {
        PlayerMoveEvent self = this.paperarc$self();
        return self.hasExplicitlyChangedBlock()
                || !self.getFrom().getWorld().equals(self.getTo().getWorld());
    }

    @Unique
    public boolean hasChangedOrientation() {
        Location from = this.paperarc$self().getFrom();
        Location to = this.paperarc$self().getTo();
        return from.getPitch() != to.getPitch() || from.getYaw() != to.getYaw();
    }
}
