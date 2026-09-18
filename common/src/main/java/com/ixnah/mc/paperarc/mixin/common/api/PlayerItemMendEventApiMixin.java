package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.IntUnaryOperator;

/**
 * paper 的 {@code PlayerItemMendEvent#getDurabilityToXpOperation()/setDurabilityToXpOperation/
 * getConsumedExperience()}（Expand-PlayerItemMendEvent.patch）。
 *
 * <p>默认算子就是 {@code ExperienceOrb#durabilityToXp}（1.20.1 里正是 {@code amount / 2}，
 * srg jar javap 核对），与 paper 的旧构造器默认值一致。
 *
 * <p>消费方见 {@code ExperienceOrbMendMixin}：
 * {@code repairPlayerItems} 里扣多少经验改由这个算子说了算。
 */
@Mixin(PlayerItemMendEvent.class)
public abstract class PlayerItemMendEventApiMixin {

    @Shadow
    public abstract int getRepairAmount();

    @Unique
    private IntUnaryOperator paperarc$durabilityToXpOp = amount -> amount / 2;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/inventory/ItemStack;Lorg/bukkit/inventory/EquipmentSlot;Lorg/bukkit/entity/ExperienceOrb;I)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$register(Player who, ItemStack item, EquipmentSlot slot, ExperienceOrb experienceOrb,
                                   int repairAmount, CallbackInfo ci) {
        this.paperarc$durabilityToXpOp = amount -> amount / 2;
        EventCauseState.setLastMendEvent((PlayerItemMendEvent) (Object) this);
    }

    @Unique
    public IntUnaryOperator getDurabilityToXpOperation() {
        return this.paperarc$durabilityToXpOp;
    }

    @Unique
    public void setDurabilityToXpOperation(IntUnaryOperator durabilityToXpOp) {
        Preconditions.checkNotNull(durabilityToXpOp);
        this.paperarc$durabilityToXpOp = durabilityToXpOp;
    }

    @Unique
    public int getConsumedExperience() {
        return this.paperarc$durabilityToXpOp.applyAsInt(this.getRepairAmount());
    }
}
