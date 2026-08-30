package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.ixnah.mc.paperarc.bridge.BeehiveBlockEntityBridge;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/**
 * Injects Paper's {@code BeehiveBlockEntity.clearBees()} as a bridge-backed method.
 * Vanilla has no such method; Paper's Add-EntityBlockStorage-clearEntities.patch adds
 * {@code public void clearBees() { this.stored.clear(); }}.
 */
@Mixin(BeehiveBlockEntity.class)
public abstract class BeehiveBlockEntityFieldsMixin implements BeehiveBlockEntityBridge {

    @Shadow
    private java.util.List stored;

    @Override
    public void paper$clearEntities() {
        this.stored.clear();
    }
}
