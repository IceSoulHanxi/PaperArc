package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code PlayerMoveEvent} 上的 5 个判定方法（checklist §1.10 am，第 ④ 批）。
 * 全部由 {@code getFrom()/getTo()} 现算，没有额外状态，语义与 paper 完全一致。
 */
@Mixin(PlayerMoveEvent.class)
public abstract class PlayerMoveEventApiMixin {

    @Unique
    private Location paperarc$from() {
        return ((PlayerMoveEvent) (Object) this).getFrom();
    }

    @Unique
    private Location paperarc$to() {
        return ((PlayerMoveEvent) (Object) this).getTo();
    }

    @Unique
    public boolean hasExplicitlyChangedPosition() {
        Location from = this.paperarc$from();
        Location to = this.paperarc$to();
        return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
    }

    @Unique
    public boolean hasChangedPosition() {
        return this.hasExplicitlyChangedPosition()
                || !this.paperarc$from().getWorld().equals(this.paperarc$to().getWorld());
    }

    @Unique
    public boolean hasExplicitlyChangedBlock() {
        Location from = this.paperarc$from();
        Location to = this.paperarc$to();
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }

    @Unique
    public boolean hasChangedBlock() {
        return this.hasExplicitlyChangedBlock()
                || !this.paperarc$from().getWorld().equals(this.paperarc$to().getWorld());
    }

    @Unique
    public boolean hasChangedOrientation() {
        Location from = this.paperarc$from();
        Location to = this.paperarc$to();
        return from.getPitch() != to.getPitch() || from.getYaw() != to.getYaw();
    }
}
