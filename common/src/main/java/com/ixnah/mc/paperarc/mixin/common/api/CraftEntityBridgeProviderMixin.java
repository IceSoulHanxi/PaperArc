package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftEntityBridge;
import net.minecraft.world.entity.Entity;
import org.bukkit.World;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Merges {@link CraftEntityBridge} onto {@code CraftEntity} so subclass api
 * mixins reach handle/world/update via duck typing.
 */
@Mixin(CraftEntity.class)
public abstract class CraftEntityBridgeProviderMixin implements CraftEntityBridge {

    @Shadow
    public abstract Entity getHandle();

    @Override
    public Entity paperarc$getHandle() {
        return this.getHandle();
    }

    @Shadow
    public abstract World getWorld();

    @Override
    public World paperarc$getWorld() {
        return this.getWorld();
    }

    @Shadow
    protected abstract void update();

    @Override
    public void paperarc$update() {
        this.update();
    }
}
